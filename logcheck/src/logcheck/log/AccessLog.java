package logcheck.log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.inject.Inject;

public class AccessLog {

	@Inject private Logger log;

	public static final String PATTERN = "(\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d) - ([\\w-]+) - \\[([\\d\\.]*)\\] (.+)\\(([\\w\\(\\)-]*)\\)\\[(.*)\\] - (.*)$";
	public static final String PATTERN2 = "\\[\\d+\\.\\d+\\.\\d+\\.\\d+\\] ([\\S ]+)\\([\\S ]*\\)\\[[\\S ]*\\]";

	public AccessLog() {
		// なにもしない
	}

	public Map<String, AccessLogSummary> load(String file) {
		HashMap<String, AccessLogSummary> map = new HashMap<>();
		try {
			log.log(Level.INFO, "start load ... file={0}", file);

			try (Stream<String> input = Files.lines(Paths.get(file), StandardCharsets.UTF_8)) {
				input//.filter(AccessLog::test)
					.map(AccessLog::parse)
					.filter(Objects::nonNull)
					.forEach(b -> {
						AccessLogSummary als = map.get(b.getAddr().toString());
						if (als == null) {
							als = new AccessLogSummary(b, null);
							map.put(b.getAddr().toString(), als);
						}
					});
			}
			log.log(Level.INFO, "end load ... size={0}", map.size());

		} catch (IOException e) {
			log.log(Level.SEVERE, e.getMessage());
		}
		return map;
	}

	public static AccessLogBean parse(String s) {
		if (s.startsWith("#")) {
			return null;
		}

		String[] array = s.split(" - ");
		if (array.length < 4) {
			Logger.getLogger(AccessLog.class.getName()).log(Level.WARNING, "(AccessLog): \"{0}\"", s);
			return null;
		}

		Pattern p = Pattern.compile(PATTERN2);
		Matcher m = p.matcher(array[2]);
		if (!m.matches()) {
			Logger.getLogger(AccessLog.class.getName()).log(Level.WARNING, "(AccessLog): \"{0}\"", s);
			return null;
		}

		String date = array[0];
		String host = array[1];
		int pos1 = array[2].indexOf(']', 1);
		int pos2 = array[2].indexOf(' ', pos1 + 1);
		int pos3 = array[2].indexOf('(', pos2 + 1);
		int pos4 = array[2].indexOf('[', pos3 + 1);
		String ip = array[2].substring(1, pos1);
		String id = array[2].substring(pos2 + 1, pos3);
		// "SDC\"始まりの場合、以降の処理で都合の悪い場合があるので"SDC\"は削除する
		if (id.startsWith("SDC\\") && id.length() > 4) {
			id = id.substring(4);
		}
		// 2018/03/08 AccessLogBeanからZユーザの大文字置換を移した
		id = id.toUpperCase();

		String role = array[2].substring(pos4 + 1, array[2].length() - 1);
		String msg = IntStream.range(3, array.length)
			.mapToObj(n -> array[n])
			.collect(Collectors.joining(" - "));
		return new AccessLogBean(date, host, ip, id, role, msg);
	}

}
