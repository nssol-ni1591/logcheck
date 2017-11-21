package logcheck.fw;

import java.util.logging.Logger;

public class FwLog {

	private static final Logger log = Logger.getLogger(FwLog.class.getName());

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
				// 値に空白が含まれるログ対応：例えば、"United State"など
//				throw new IllegalArgumentException("index < 0 : st=" + st);
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
		/* test（）にcheckを入れたので、parseでの以下の条件は不要（なはず）
		if (srcip == null) {
			throw new IllegalArgumentException("srcip is null: \"" + s + "\"");
		}
		else if (dstip == null) {
			throw new IllegalArgumentException("desip is null: \"" + s + "\"");
		}
		*/
		return new FwLogBean(date, time, level, srcip, srcport, dstip, dstport);
	}

	public static boolean test(String s) {
		if (s.startsWith("#")) {
			log.warning("(FwLog): \"" + s + "\"");
			return false;
		}
		if (!s.contains("srcip=") || !s.contains("dstip=")) {
			log.warning("(FwLog): \"" + s + "\"");
			return false;
		}
		return true;
	}

}
