package logcheck.sdc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logcheck.annotations.WithElaps;
import logcheck.util.NetAddr;

public class SdcList extends LinkedHashMap<String, SdcListIsp> {

	private static Logger log = Logger.getLogger(SdcList.class.getName());

	private static final long serialVersionUID = 1L;
	public static final String PATTERN = "(\\d+\\.\\d+\\.\\d+\\.\\d+/?[\\d\\.]*)\t([\\S ]+)\t([\\S ]+)";

	public SdcList() {
		super(100);
	}

	public SdcListIsp get(NetAddr addr) {
		Optional<SdcListIsp> rc = values().stream().filter(isp -> {
			return isp.getAddress().stream().filter(net -> net.within(addr)).findFirst().isPresent();
		}).findFirst();
		return rc.isPresent() ? rc.get() : null;
	}

	@WithElaps
	public SdcList load(String file) throws IOException {
		Files.lines(Paths.get(file), Charset.forName("MS932"))
				.filter(SdcList::test)
				.map(SdcList::parse)
				.forEach(b -> {
					SdcListIsp isp = get(b.getName());
					if (isp == null) {
						isp = new SdcListIsp(b.getName(), b.getType());
						put(b.getName(), isp);
					}
					isp.addAddress(new NetAddr(b.getAddr()));
				});
		return this;
	}

	public static SdcListBean parse(String s) {
		String addr = null;
		String name = null;
		String type = null;

		Pattern p = Pattern.compile(PATTERN);
		Matcher m = p.matcher(s);		// 1文字目が欠ける対策
		if (m.find(1)) {
			addr = m.group(1);
		}
		if (m.find(2)) {
			name = m.group(2);
		}
		if (m.find(3)) {
			type = m.group(3);
		}
		return new SdcListBean(name, addr, type);
	}
	public static boolean test(String s) {
		if (s.startsWith("#")) {
			return false;
		}

		Pattern p = Pattern.compile(PATTERN);
		Matcher m = p.matcher(s);
		boolean rc = m.find();
		if (!rc) {
//			System.err.println("WARNING(SDC): " + s);
			log.warning("(SdcList): \"" + s + "\"");
		}
		return rc;
	}

}
