package logcheck.sdc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import logcheck.annotations.WithElaps;
import logcheck.util.NetAddr;

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
			input//.filter(SdcList::test)
				.map(SdcList::parse)
				.filter(Objects::nonNull)
				.forEach(b -> {
					final SdcListIsp isp = new SdcListIsp(b.getName(), b.getType());
					add(isp);
					isp.addAddress(new NetAddr(b.getAddr()));
				});
		}
		return this;
	}

	public static SdcListBean parse(String s) {
		if (s.startsWith("#")) {
			return null;
		}

		Pattern p = Pattern.compile(PATTERN);
		Matcher m = p.matcher(s);
		if (!m.matches()) {
			Logger.getLogger(SdcList.class.getName()).log(Level.WARNING, "(SdcList): \"{0}\"", s);
			return null;
		}

		String addr = m.group(1);
		String name = m.group(2);
		String type = m.group(3);

		return new SdcListBean(name, addr, type);
	}

}
