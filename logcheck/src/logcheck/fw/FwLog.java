package logcheck.fw;

import java.util.logging.Level;
import java.util.logging.Logger;

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
			int index = st.indexOf('=');
			if (index < 0) {
				// 値に空白が含まれるログ対応：例えば、"United State"など
				continue;
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
			default:
				// skip
				break;
			}
		}
		return new FwLogBean(date, time, level, srcip, srcport, dstip, dstport);
	}

	public static boolean test(String s) {
		final Logger log = Logger.getLogger(FwLog.class.getName());
		if (s.startsWith("#")) {
			return false;
		}
		if (!s.contains("srcip=") || !s.contains("dstip=")) {
			// type=eventの場合はログの出力は行わない
			if (!s.contains("type=event")) {
				log.log(Level.WARNING, "(FwLog): \"{0}\"", s);
			}
			return false;
		}
		return true;
	}

}
