package logcheck.sdc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import logcheck.annotations.WithElaps;
import logcheck.util.net.NetAddr;

public class SdcList extends ArrayList<SdcListIsp> {

	private static final long serialVersionUID = 1L;
	public static final String PATTERN = "(\\d+\\.\\d+\\.\\d+\\.\\d+/?[\\d\\.]*)\t([\\S ]+)\t([\\S ]+)(\t[\\S ]*)?";

	public SdcList() {
		super(200);
	}

	public SdcListIsp get(NetAddr addr) {
		Optional<SdcListIsp> rc = stream()
				.filter(isp -> isp.within(addr))
				.findFirst();
		return rc.isPresent() ? rc.get() : null;
	}

	@WithElaps
	public SdcList load(String file) throws IOException {
		try (Stream<String> input = Files.lines(Paths.get(file), Charset.forName("MS932"))) {
			input.filter(SdcList::test)
				.map(SdcList::parse)
				.forEach(b -> {
					final SdcListIsp isp = new SdcListIsp(b.getName(), b.getType());
					add(isp);
					isp.addAddress(new NetAddr(b.getAddr()));
				});
		}
		return this;
	}

	public static SdcListBean parse(String s) {
		String addr = null;
		String name = null;
		String type = null;

		Pattern p = Pattern.compile(PATTERN);
		Matcher m = p.matcher(" " + s);		// 1文字目が欠ける対策
		//Matcher m = p.matcher(s);		// 1文字目が欠ける対策
		if (m.find(1)) {
			addr = m.group(1);
		}
		if (m.find(2)) {
			name = m.group(2);
		}
		if (m.find(3)) {
			type = m.group(3);
		}
		if (name == null || addr == null || type == null) {
			Logger.getLogger(SdcList.class.getName())
				.log(Level.WARNING, "(SdcList): addr={0}, name={1}, type={2}", new Object[] { addr, name, type });
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
			// JUnitの場合、logのインスタンスが生成できないため
			Logger.getLogger(SdcList.class.getName()).log(Level.WARNING, "(SdcList): \"{0}\"", s);
		}
		return rc;
	}

}
