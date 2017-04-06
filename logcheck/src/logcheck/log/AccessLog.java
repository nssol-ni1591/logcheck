package logcheck.log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccessLog /*extends HashMap<String, AccessLogSummary>*/ {

	public static final String PATTERN = "(\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d) - ([\\w-]+) - \\[([\\d\\.]*)\\] (.+)\\(([\\w\\(\\)-]*)\\)\\[(.*)\\] - (.*)$";
//	public static String PATTERN = "(\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d) - ([\\w-]+) - \\[([\\d\\.]*)\\] (.+)";
//	public static final String PATTERN2 = "\\[(\\d+\\.\\d+\\.\\d+\\.\\d)\\] \\w+\\([\\w-\\(\\)]+\\)\\[[\\S ]*\\]";
	public static final String PATTERN2 = "\\[\\d+\\.\\d+\\.\\d+\\.\\d+\\] ([\\S ])+\\(\\S*\\)\\[[\\S ]*\\]";

	private AccessLog() { }
	/*
	public static AccessLog load(String file) throws IOException {
		AccessLog map = new AccessLog();
		Files.lines(Paths.get(file), StandardCharsets.UTF_8)
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.forEach(alb -> {
					AccessLogSummary als = map.get(alb.getIp());
					if (als == null) {
						als = new AccessLogSummary(alb.getIp());
						map.put(alb.getIp(), als);
					}
					als.addCount();
					//als.add(alb);
				});
		return map;
	}
	 */
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
	/*
	public static AccessLogBean parse(String s) {
		String date = null;
		String host = null;
		String ip = null;
		String id = null;
		String role = null;
		String msg = null;

		Pattern p = Pattern.compile(AccessLog.PATTERN2);
		Matcher m = p.matcher("   " + s);		// 1文字目が欠ける対策
		if (m.find(1)) {
			date = m.group(1);
		}
		if (m.find(2)) {
			host = m.group(2);
		}
		if (m.find(3)) {
			ip = m.group(3);
		}
		if (m.find(4)) {
			id = m.group(4);
		}
		if (m.find(5)) {
			role = m.group(5);
		}
		if (m.find(6)) {
			msg = m.group(6);
		}
		return new AccessLogBean(date, host, ip, id, role, msg);
	}
	*/
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
	/*
	public static boolean test2(String s) {
		if (!test(s)) {
			return false;
		}
		return s.contains("fail");
	}
	*/
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
							//als.add(b);
							System.out.println("log=" + b);
						});
				/*
				for (String ipaddr : map.keySet()) {
					AccessLogSummary als = map.get(ipaddr);
					System.out.println(ipaddr + " : " + als.sum());
				}
				*/
				System.out.println("end AccessLog.main ...");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("AccessLog.main ... end");
	}
}
