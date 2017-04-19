package logcheck.mag.tsv;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logcheck.mag.MagList;
import logcheck.mag.MagListBean;
import logcheck.mag.MagListIsp;
import logcheck.util.NetAddr;

public class TsvMagList extends HashMap<String, MagListIsp> implements MagList {

	private static final long serialVersionUID = 1L;
	//public static String PATTERN = "(PRJ_[\\w_]+)\t(^\t+)\t(^\t+)\t([\\d\\.]+)\t([\\d\\.]+)\t([\\d\\.]+)";
	//public static String PATTERN = "(PRJ_[\\w_]+)\t(.+)\t(.+)\t(.+)\t([\\d+\\.\\d+\\.\\d+\\.\\d+]+)\t([\\d+\\.\\d+\\.\\d+\\.\\d+]+)";
	//public static String PATTERN = "(PRJ_[\\w_]+)\t(.+)\t(.+)\t([\\d+\\\\.\\d+\\\\.\\d+\\\\.\\d+]+)\t([\\d+\\\\.\\d+\\\\.\\d+\\\\.\\d+]+)\t([\\d+\\\\.\\d+\\\\.\\d+\\\\.\\d+]+)";
	public static String PATTERN = "(PRJ_[\\w_]+)\t(.+)\t(.+)\t([\\d\\.～\\/]+)\t([\\d+\\.\\d+\\.\\d+\\.\\d+]+)\t([\\d+\\.\\d+\\.\\d+\\.\\d+]+)";

	public TsvMagList() { }
	/*
	public HashMap<NetAddr, String> listAddress() {
		HashMap<NetAddr, String> map = new HashMap<>();
		values().forEach(isp -> {
			isp.getAddress().forEach(addr -> map.put(addr, isp.getName()));
		});
		return map;
	}
	*/
	/*
	 * 引数のIPアドレスを含むCompanyを取得する
	 */
	public MagListIsp get(NetAddr addr) {
		Optional<MagListIsp> rc = values().stream().filter(isp -> {
			return isp.getAddress().stream().filter(net -> net.within(addr)).findFirst().isPresent();
		}).findFirst();
		return rc.isPresent() ? rc.get() : null;
	}

//	public static TsvMagList load(String file) throws IOException {
	public TsvMagList load(String file) throws IOException {
//		TsvMagList map = new TsvMagList();
		Files.lines(Paths.get(file), Charset.forName("MS932"))
//				.filter(TsvMagList::test)
				.filter(s -> test(s))
//				.map(TsvMagList::parse)
				.map(s -> parse(s))
				.forEach(b -> {
					MagListIsp mp = this.get(b.getPrjId());
					if (mp == null) {
						mp = new MagListIsp(b.getPrjId());
						this.put(b.getPrjId(), mp);
					}
					NetAddr addr = new NetAddr(b.getMagIp());
					mp.addAddress(addr);
					//System.out.printf("prjId=%s, addr=%s\n", b.getPrjId(), addr);
				});
		return this;
	}
//	public static MagListBean parse(String s) {
	private MagListBean parse(String s) {
		String prjId = null;
		String prjName = null;
		String prjSite = null;
		String prjIp = null;
		String magIp = null;
		String magMask = null;

		String[] array = s.split("\t");
		if (array.length > 1) {
			prjId = array[1];
		}
		if (array.length > 2) {
			prjName = array[2];
		}
		if (array.length > 3) {
			prjSite = array[3];
		}
		if (array.length > 4) {
			prjIp = array[4];
		}
		if (array.length > 5) {
			magIp = array[5];
		}
		if (array.length > 6) {
			magMask = array[6];
		}
		return new MagListBean(prjId, prjName, prjSite, prjIp, magIp, magMask);
	}
	/*
	public static MagListBean parse(String s) {
		String prjId = null;
		String prjName = null;
		String prjConn = null;
		String prjIp = null;
		String magIp = null;
		String magMask = null;

		Pattern p = Pattern.compile(MagList.PATTERN);
		Matcher m = p.matcher("   " + s);		// 1文字目が欠ける対策
		if (m.find(1)) {
			prjId = m.group(1);
		}
		if (m.find(2)) {
			prjName = m.group(2);
		}
		if (m.find(3)) {
			prjConn = m.group(3);
		}
		if (m.find(4)) {
			prjIp = m.group(4);
		}
		if (m.find(5)) {
			magIp = m.group(5);
		}
		if (m.find(6)) {
			magMask = m.group(6);
		}
		return new MagListBean(prjId, prjName, prjConn, prjIp, magIp, magMask);
	}
	*/
//	public static boolean test(String s) {
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
			System.err.println("WARNING: " + s.trim());
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