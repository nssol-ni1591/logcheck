package logcheck.sdc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import logcheck.annotations.UseSdcListNW;
import logcheck.annotations.WithElaps;
import logcheck.util.net.NetAddr;

/*
 * Checker50のsdclistの対象をNetworkアドレスに限定しました.
 * つまり、ホスト（ネットマスク32bit）の定義はエラーとしてコンソールに出力される.
 */
@UseSdcListNW
public class SdcListNW extends SdcList {

	private static final long serialVersionUID = 1L;
	public static final String PATTERN = "(\\d+\\.\\d+\\.\\d+\\.\\d+/[\\d\\.]+)\t([\\S ]+)\t([\\S ]+)(\t[\\S ]*)?";

	@WithElaps
	public SdcList load(String file) throws IOException {
		try (Stream<String> input = Files.lines(Paths.get(file), Charset.forName("MS932"))) {
			input.filter(SdcListNW::test)
				.map(SdcList::parse)
				.forEach(b -> {
					final SdcListIsp isp = new SdcListIsp(b.getName(), b.getType());
					add(isp);
					isp.addAddress(new NetAddr(b.getAddr()));
				});
		}
		return this;
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
			Logger.getLogger(SdcListNW.class.getName()).log(Level.WARNING, "(SdcList): \"{0}\"", s);
		}
		return rc;
	}

}
