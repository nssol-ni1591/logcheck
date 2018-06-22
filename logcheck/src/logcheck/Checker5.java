package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.inject.Inject;

import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.log.AccessLogBean;
import logcheck.mag.MagList;
import logcheck.util.net.NetAddr;
import logcheck.util.weld.WeldWrapper;

/*
 * 国 > ISP > メッセージ > クライアントIP 毎にログ数を集計する
 */
public class Checker5 extends AbstractChecker<Map<String, Map<IspList, Map<String, Integer>>>> {

	@Inject private Logger log;

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	private static final Pattern[] FAIL_PATTERNS_ALL;
	static {
		FAIL_PATTERNS_ALL = new Pattern[FAIL_PATTERNS.length + FAIL_PATTERNS_DUP.length];
		System.arraycopy(FAIL_PATTERNS, 0, FAIL_PATTERNS_ALL, 0, FAIL_PATTERNS.length);
		System.arraycopy(FAIL_PATTERNS_DUP, 0, FAIL_PATTERNS_ALL, FAIL_PATTERNS.length, FAIL_PATTERNS_DUP.length);
	}

	public void init(String...argv) throws IOException, ClassNotFoundException, SQLException {
		this.knownlist.load(argv[0]);
		this.maglist.load(argv[1]);
	}

	private void sub(Map<String, Map<IspList, Map<String, Integer>>> map,
			IspList isp, AccessLogBean b, String m)
	{
		Map<IspList, Map<String, Integer>> ispmap = map.get(isp.getCountry());
		if (ispmap == null) {
			ispmap = new TreeMap<>();
			map.put(isp.getCountry(), ispmap);
		}

		Map<String, Integer> msgmap = ispmap.get(isp);
		if (msgmap == null) {
			msgmap = new TreeMap<>();
			ispmap.put(isp, msgmap);
		}

		Integer count = msgmap.get(m);
		if (count == null) {
			count = Integer.valueOf(0);
			msgmap.put(m, count);
		}
		count += 1;
		msgmap.put(m, count);		
	}
	@Override
	public Map<String, Map<IspList, Map<String, Integer>>> call(Stream<String> stream) {
		final Map<String, Map<IspList, Map<String, Integer>>> map = new TreeMap<>();
		stream.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.forEach(b -> {
					// ログのメッセージ部分はPatternの正規化表現で集約するため、対象ログが一致したPattern文字列を取得する
					Optional<String> rc = Stream.of(FAIL_PATTERNS_ALL)
							.filter(p -> p.matcher(b.getMsg()).matches())
							.map(Pattern::toString)
							.findFirst();
					String m = rc.isPresent() ? rc.get() : b.getMsg();
					if (!rc.isPresent()) {
						m = INFO_SUMMARY_MSG;
					}

					NetAddr addr = b.getAddr();
					IspList isp = maglist.get(addr);
					if (isp == null) {
						isp = knownlist.get(addr);
					}

					if (isp != null) {
						sub(map, isp, b, m);
					}
					else {
						log.log(Level.WARNING, "unknown ip: addr={0}", addr);
					}
				});
		return map;
	}

	@Override
	public void report(final PrintWriter out, final Map<String, Map<IspList, Map<String, Integer>>> map) {
		map.keySet().forEach(country -> {
			out.println();

			int sum = map.get(country).values().stream().mapToInt(msgmap -> 
				msgmap.values().stream().mapToInt(Integer::intValue).sum()
			).sum();
			out.println(("".equals(country) ? "<MAGLIST>" : country) + " : " + sum);

			map.get(country).forEach((isp, msgmap) -> {
				int sum2 = msgmap.values().stream().mapToInt(Integer::intValue).sum();
				out.println("\t" + isp.getName() + " : " + sum2);

				msgmap.keySet().forEach(msg -> 
					out.println(new StringBuilder().append("\t\t[ ").append(msg).append(" ] : ").append(msgmap.get(msg)))
				);
			});
		});
		out.println();
	}

	public static void main(String ... argv) {
		int rc = new WeldWrapper<Checker5>(Checker5.class).weld(2, argv);
		System.exit(rc);
	}
}
