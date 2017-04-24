package logcheck.log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccessLog {

	public static final String PATTERN = "(\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d) - ([\\w-]+) - \\[([\\d\\.]*)\\] (.+)\\(([\\w\\(\\)-]*)\\)\\[(.*)\\] - (.*)$";
	public static final String PATTERN2 = "\\[\\d+\\.\\d+\\.\\d+\\.\\d+\\] ([\\S ])+\\(\\S*\\)\\[[\\S ]*\\]";

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
			System.err.println("SKIP: \"" + s + "\"");
			return false;
		}

		String[] array = s.split(" - ");
		if (array.length < 4) {
			System.err.println("ERROR: \"" + s + "\"");
			return false;
		}

		Pattern p = Pattern.compile(PATTERN2);
		Matcher m = p.matcher(array[2]);
		boolean rc = m.matches();
		if (!rc) {
			System.err.println("ERROR: \"" + s + "\"");
		}
		return rc;
	}

	public static void main(String... argv) {
		try {
			for (String file : argv) {
				System.out.println("start AccessLog.main ... file=" + file);
				HashMap<String, AccessLogSummary> map = new HashMap<>();
				Files.lines(Paths.get(file), StandardCharsets.UTF_8)
						.filter(AccessLog::test)
						.map(AccessLog::parse)
						.forEach(b -> {
							AccessLogSummary als = map.get(b.getAddr().toString());
							if (als == null) {
								als = new AccessLogSummary(b.getAddr().toString());
								map.put(b.getAddr().toString(), als);
							}
							als.addAddress(b.getAddr());
							System.out.println("log=" + b);
						});
				System.out.println("end AccessLog.main ...");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("AccessLog.main ... end");
	}
}
