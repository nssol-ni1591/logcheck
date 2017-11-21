package logcheck.log;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccessLog {

	private static Logger log = Logger.getLogger(AccessLog.class.getName());

	public static final String PATTERN = "(\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d) - ([\\w-]+) - \\[([\\d\\.]*)\\] (.+)\\(([\\w\\(\\)-]*)\\)\\[(.*)\\] - (.*)$";
// for 2017-11-18
//	public static final String PATTERN2 = "\\[\\d+\\.\\d+\\.\\d+\\.\\d+\\] ([\\S ])+\\(\\S*\\)\\[[\\S ]*\\]";
	public static final String PATTERN2 = "\\[\\d+\\.\\d+\\.\\d+\\.\\d+\\] ([\\S ]+)\\([\\S ]*\\)\\[[\\S ]*\\]";

	private AccessLog() { }

	public static AccessLogBean parse(String s) {
		String date = null;
		String host = null;
		String ip = null;
		String id = null;
		String role = null;
		StringBuilder msg = null;

		String[] array = s.split(" - ");
		date = array[0];
		host = array[1];
		int pos1 = array[2].indexOf("]", 1);
		int pos2 = array[2].indexOf(" ", pos1 + 1);
		int pos3 = array[2].indexOf("(", pos2 + 1);
		int pos4 = array[2].indexOf("[", pos3 + 1);
		ip = array[2].substring(1, pos1);
		id = array[2].substring(pos2 + 1, pos3);
		role = array[2].substring(pos4 + 1, array[2].length() - 1);
		msg = new StringBuilder(array[3]);
		for (int ix = 4; ix < array.length; ix++) {
			msg.append(" - ").append(array[ix]).toString();
		}
		return new AccessLogBean(date, host, ip, id, role, msg.toString());
	}

	public static boolean test(String s) {
		if (s.startsWith("#")) {
			return false;
		}

		String[] array = s.split(" - ");
		if (array.length < 4) {
			log.warning("(AccessLog): \"" + s + "\"");
			return false;
		}

		Pattern p = Pattern.compile(PATTERN2);
		Matcher m = p.matcher(array[2]);
		boolean rc = m.matches();
		if (!rc) {
			log.warning("(AccessLog): \"" + s + "\"");
		}
		return rc;
	}

}
