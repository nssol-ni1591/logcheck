package logcheck.mag.tsv;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import logcheck.annotations.WithElaps;
import logcheck.mag.MagList;
import logcheck.mag.MagListBean;
import logcheck.mag.MagListIsp;
import logcheck.util.NetAddr;

@Alternative
public class TsvMagList extends HashMap<String, MagListIsp> implements MagList {

	@Inject private Logger log;

	private static final long serialVersionUID = 1L;

	public static String PATTERN = "(PRJ_[\\w_]+)\t(.+)\t(.+)\t([\\d\\.～\\/]+)\t([\\d+\\.\\d+\\.\\d+\\.\\d+]+)\t([\\d+\\.\\d+\\.\\d+\\.\\d+]+)";

	public TsvMagList() {
		super(200);
	}

	/*
	 * 引数のIPアドレスを含むCompanyを取得する
	 */
	public MagListIsp get(NetAddr addr) {
		Optional<MagListIsp> rc = values().stream().filter(isp -> {
			return isp.getAddress().stream().filter(net -> net.within(addr)).findFirst().isPresent();
		}).findFirst();
		return rc.isPresent() ? rc.get() : null;
	}

	@WithElaps
	public TsvMagList load(String file) throws IOException {
		Files.lines(Paths.get(file), Charset.forName("MS932"))
				.filter(s -> test(s))
				.map(s -> parse(s))
				.forEach(b -> {
					MagListIsp mp = this.get(b.getProjId());
					if (mp == null) {
						mp = new MagListIsp(b.getProjId());
						this.put(b.getProjId(), mp);
					}
					NetAddr addr = new NetAddr(b.getMagIp());
					mp.addAddress(addr);
				});
		return this;
	}

	private MagListBean parse(String s) {
		String projId = null;
		String projName = null;
		String projSite = null;
		String projIp = null;
		String magIp = null;
		String magMask = null;

		String[] array = s.split("\t");
		if (array.length > 1) {
			projId = array[1];
		}
		if (array.length > 2) {
			projName = array[2];
		}
		if (array.length > 3) {
			projSite = array[3];
		}
		if (array.length > 4) {
			projIp = array[4];
		}
		if (array.length > 5) {
			magIp = array[5];
		}
		if (array.length > 6) {
			magMask = array[6];
		}
		return new MagListBean(projId, projName, projSite, projIp, magIp, magMask);
	}
	private boolean test(String s) {
		if (s.startsWith("#")) {
			return false;
		}
		if (s.startsWith("\t\t")) {
			return false;
		}

		Pattern p = Pattern.compile(PATTERN);
		Matcher m = p.matcher(s);
		boolean rc = m.find();
		if (!rc) {
//			System.err.println("WARNING(MAG): " + s.trim());
			log.warning("(インターネット経由接続先): s=\"" + s.trim() + "\"");
		}

		String[] array = s.split("\t");
		if (array.length > 5 && "非固定".equals(array[5])) {
			rc = false;
		}
		if (array.length > 6 && "非固定".equals(array[6])) {
			rc = false;
		}
		return rc;
	}

	public static void main(String... argv) {
		System.out.println("start TsvMagList.main ...");
		TsvMagList map = new TsvMagList();
		try {
			map = new TsvMagList().load(argv[0]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String name : map.keySet()) {
			MagListIsp c = map.get(name);
			System.out.println(name + "=" + c.getAddress());
		}
		System.out.println("TsvMagList.main ... end");
	}
}
