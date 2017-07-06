package logcheck;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.mag.MagList;
import logcheck.util.net.NetAddr;

/*
 * 国 > ISP > メッセージ > クライアントIP 毎にログ数を集計する
 */
public class Checker5 extends AbstractChecker<Map<String, Map<IspList, Map<String, Integer>>>> {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

//	private Map<String, Map<IspList, Map<String, Integer>>> map = new TreeMap<>();

	private static final Pattern[] FAIL_PATTERNS_ALL;
	static {
		FAIL_PATTERNS_ALL = new Pattern[FAIL_PATTERNS.length + FAIL_PATTERNS_DUP.length];
		System.arraycopy(FAIL_PATTERNS, 0, FAIL_PATTERNS_ALL, 0, FAIL_PATTERNS.length);
		System.arraycopy(FAIL_PATTERNS_DUP, 0, FAIL_PATTERNS_ALL, FAIL_PATTERNS.length, FAIL_PATTERNS_DUP.length);
	}

	public Checker5 init(String knownfile, String magfile) throws Exception {
		this.knownlist.load(knownfile);
		this.maglist.load(magfile);
		return this;
	}

	@Override
	public Map<String, Map<IspList, Map<String, Integer>>> call(Stream<String> stream) throws Exception {
		final Map<String, Map<IspList, Map<String, Integer>>> map = new TreeMap<>();
		stream.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.forEach(b -> {
					// ログのメッセージ部分はPatternの正規化表現で集約するため、対象ログが一致したPattern文字列を取得する
					Optional<String> rc = Stream.of(FAIL_PATTERNS_ALL)
							.filter(p -> p.matcher(b.getMsg()).matches())
							.map(p -> p.toString())
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
							count = new Integer(0);
							msgmap.put(m, count);
						}
						count += 1;
						msgmap.put(m, count);
					}
					else {
						System.err.println("unknown ip: addr=" + addr);
					}
				});
		return map;
	}

	@Override
	public void report(final Map<String, Map<IspList, Map<String, Integer>>> map) {
		map.keySet().forEach(country -> {
			System.out.println();

			int sum = map.get(country).values().stream().mapToInt(msgmap -> {
				return msgmap.values().stream().mapToInt(c -> c.intValue()).sum();
			}).sum();
			System.out.println(("".equals(country) ? "<MAGLIST>" : country) + " : " + sum);

			map.get(country).forEach((isp, msgmap) -> {
				int sum2 = msgmap.values().stream().mapToInt(c -> c.intValue()).sum();
				System.out.println("\t" + isp.getName() + " : " + sum2);

				msgmap.keySet().forEach(msg -> {
					System.out.println(new StringBuilder().append("\t\t[ ").append(msg).append(" ] : ").append(msgmap.get(msg)));
				});
			});
		});
		System.out.println();
	}

	public static void main(String ... argv) {
		if (argv.length < 2) {
			System.err.println("usage: java logcheck.Checker4 knownlist maglist [accesslog...]");
			System.exit(1);
		}

		System.setProperty("java.util.logging.config.class", "logcheck.util.LogConfig");
		System.setProperty("file.encoding", "UTF-8");

		int rc = 0;
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker5 application = container.instance().select(Checker5.class).get();
			application.init(argv[0], argv[1]).start(argv, 2);
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
			rc = 1;
		}
		System.exit(rc);
	}
}
