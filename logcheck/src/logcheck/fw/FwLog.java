package logcheck.fw;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

public class FwLog {

	private FwLog() { }

	/*
	 * どうparseするかが問題：regexか、susstringか
	 */
	public static FwLogBean parse(String s) {
		String date = null;
		String time = null;
		String level = null;
		String srcip = null;
		String srcport = null;
		String dstip = null;
		String dstport = null;

		String[] array = s.split(" ");
		for (String st: array) {
			int index = st.indexOf("=");
			if (index < 0) {
				//throw new IllegalArgumentException("index < 0 : st=" + st);
				break;
			}
			String key = st.substring(0, index);
			switch (key) {
			case "date":
				date = st.substring(index + 1);
				break;
			case "time":
				time = st.substring(index + 1);
				break;
			case "level":
				level = st.substring(index + 1);
				break;
			case "srcip":
				srcip = st.substring(index + 1);
				break;
			case "srcport":
				srcport = st.substring(index + 1);
				break;
			case "dstip":
				dstip = st.substring(index + 1);
				break;
			case "dstport":
				dstport = st.substring(index + 1);
				break;
			}
		}
		return new FwLogBean(date, time, level, srcip, srcport, dstip, dstport);
	}

	public static boolean test(String s) {
		if (s.startsWith("#")) {
			System.err.println("SKIP: \"" + s + "\"");
			return false;
		}
		return true;
	}

	public static void main(String... argv) {
		System.err.println("start FwLog.main ... ");
		Map<FwLogBean, FwLogSummary> map = new TreeMap<>();
		try {
			for (String file : argv) {
				System.err.println("FwLog.main: loading file=" + file);
				Files.lines(Paths.get(file), StandardCharsets.UTF_8)
						.filter(FwLog::test)
						.map(FwLog::parse)
						.forEach(b -> {
							FwLogSummary summary = map.get(b);
							if (summary == null) {
								summary = new FwLogSummary(b);
								map.put(b,  summary);
							}
							else {
								summary.update(b);
							}
						});
			}
			
			map.values().forEach(summary -> System.out.println("log: " + summary));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println("end FwLog.main ...");
		System.exit(0);
	}
}
