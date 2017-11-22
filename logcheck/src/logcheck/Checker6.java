package logcheck;

import java.io.PrintWriter;
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
public class Checker6 extends AbstractChecker<Map<String, Map<IspList, Map<String, Map<NetAddr, Integer>>>>> {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	private static final Pattern[] FAIL_PATTERNS_ALL;
	static {
		FAIL_PATTERNS_ALL = new Pattern[FAIL_PATTERNS.length + FAIL_PATTERNS_DUP.length];
		System.arraycopy(FAIL_PATTERNS, 0, FAIL_PATTERNS_ALL, 0, FAIL_PATTERNS.length);
		System.arraycopy(FAIL_PATTERNS_DUP, 0, FAIL_PATTERNS_ALL, FAIL_PATTERNS.length, FAIL_PATTERNS_DUP.length);
	}

	public Checker6 init(String knownfile, String magfile) throws Exception {
		this.knownlist.load(knownfile);
		this.maglist.load(magfile);
		return this;
	}

	@Override
	public Map<String, Map<IspList, Map<String, Map<NetAddr, Integer>>>> call(Stream<String> stream) throws Exception {
		final Map<String, Map<IspList, Map<String, Map<NetAddr, Integer>>>> map = new TreeMap<>();
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
						Map<IspList, Map<String, Map<NetAddr, Integer>>> ispmap;
						Map<String, Map<NetAddr, Integer>> msgmap;
						Map<NetAddr, Integer> addrmap;
						Integer count;

						ispmap = map.get(isp.getCountry());
						if (ispmap == null) {
							ispmap = new TreeMap<>();
							map.put(isp.getCountry(), ispmap);
						}

						msgmap = ispmap.get(isp);
						if (msgmap == null) {
							msgmap = new TreeMap<>();
							ispmap.put(isp, msgmap);
						}

						addrmap = msgmap.get(m);
						if (addrmap == null) {
							addrmap = new TreeMap<>();
							msgmap.put(m, addrmap);
						}

						count = addrmap.get(addr);
						if (count == null) {
							count = Integer.valueOf(0);
						}
						count += 1;
						addrmap.put(addr, count);
					} else {
						System.err.println("unknown ip: addr=" + addr);
					}
				});
		return map;
	}

	@Override
	public void report(final PrintWriter out, final Map<String, Map<IspList, Map<String, Map<NetAddr, Integer>>>> map) {
		System.out.println();
		map.forEach((country, ispmap) -> {
			int sum = ispmap.values().stream().mapToInt(msgmap -> {
				return msgmap.values().stream().mapToInt(addrmap -> {
					return addrmap.values().stream().mapToInt(c -> c.intValue()).sum();
				}).sum();
			}).sum();
			int sum1 = ispmap.values().stream().mapToInt(msgmap -> {
				return msgmap.get(INFO_SUMMARY_MSG) == null ? 0 : msgmap.get(INFO_SUMMARY_MSG).values().stream().mapToInt(c -> c.intValue()).sum();
			}).sum();
			System.out.println(("".equals(country) ? "<MAGLIST>" : country) +
				new StringBuilder().append(" : ").append(sum - sum1).append(" / ").append(sum).append(" => ").append((sum - sum1) * 100 / sum).append("%").toString());

			ispmap.forEach((isp, msgmap) -> {
				int sum2 = msgmap.values().stream().mapToInt(addrmap -> {
					return addrmap.values().stream().mapToInt(c -> c.intValue()).sum();
				}).sum();
				int sum21 = msgmap.get(INFO_SUMMARY_MSG) == null ? 0 : msgmap.get(INFO_SUMMARY_MSG).values().stream().mapToInt(c -> c.intValue()).sum();
				System.out.println(new StringBuilder().append("\t").append(isp.getName()).append(" : ").append(sum2 - sum21).append(" / ").append(sum2).append(" => ").append((sum2 - sum21) * 100 / sum2).append("%"));

				msgmap.forEach((msg, addrmap) -> {
					int sum3 = addrmap.values().stream().mapToInt(c -> c.intValue()).sum();
					System.out.println(new StringBuilder().append("\t\t[ ").append(msg).append(" ] : ").append(sum3));

					addrmap.forEach((addr, count) -> {
						System.out.println(new StringBuilder().append("\t\t\t").append(addr).append(" : ").append(count));
					});
				});
			});
		});
		System.out.println();
	}

	public static void main(String... argv) {
		if (argv.length < 2) {
			System.err.println("usage: java logcheck.Checker4 knownlist maglist [accesslog...]");
			System.exit(1);
		}

		int rc = 0;
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker6 application = container.select(Checker6.class).get();
			application.init(argv[0], argv[1]).start(argv, 2);
		}
		catch (Exception ex) {
			rc = 1;
		}
		System.exit(rc);
	}
}
