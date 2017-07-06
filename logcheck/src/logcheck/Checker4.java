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
import logcheck.isp.IspMap;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.mag.MagList;
import logcheck.util.net.NetAddr;

/*
 * 国 > ISP > IPアドレス > メッセージ毎にログ数を集計する
 */
public class Checker4 extends AbstractChecker<Map<String ,Map<String, IspMap<Map<String, Integer>>>>> {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

//	private Map<String, Map<String, IspMap<Map<String, Integer>>>> map = new TreeMap<>();

	private static final Pattern[] FAIL_PATTERNS_ALL;
	static {
		FAIL_PATTERNS_ALL = new Pattern[FAIL_PATTERNS.length + FAIL_PATTERNS_DUP.length];
		System.arraycopy(FAIL_PATTERNS, 0, FAIL_PATTERNS_ALL, 0, FAIL_PATTERNS.length);
		System.arraycopy(FAIL_PATTERNS_DUP, 0, FAIL_PATTERNS_ALL, FAIL_PATTERNS.length, FAIL_PATTERNS_DUP.length);
	}

	public Checker4 init(String knownfile, String magfile) throws Exception {
		this.knownlist.load(knownfile);
		this.maglist.load(magfile);
		return this;
	}

	@Override
	public Map<String, Map<String, IspMap<Map<String, Integer>>>> call(Stream<String> stream) throws Exception {
		final Map<String, Map<String, IspMap<Map<String, Integer>>>> map = new TreeMap<>();
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
						System.err.println("WARNING: \"" + b + "\"");
					}

					NetAddr addr = b.getAddr();
					IspList isp = maglist.get(addr);
					if (isp == null) {
						isp = knownlist.get(addr);
					}

					if (isp != null) {
						Map<String, IspMap<Map<String, Integer>>> ispmap;
						IspMap<Map<String, Integer>> addrmap;
						Map<String, Integer> msgmap;
						Integer count;

						ispmap = map.get(isp.getCountry());
						if (ispmap == null) {
							ispmap = new TreeMap<>();
							map.put(isp.getCountry(), ispmap);
						}

						addrmap = ispmap.get(isp.getName());
						if (addrmap == null) {
							addrmap = new IspMap<>(isp.getName(), isp.getCountry());
							ispmap.put(isp.getName(), addrmap);
						}

						msgmap = addrmap.get(addr);
						if (msgmap == null) {
							msgmap = new TreeMap<>();
							addrmap.put(addr, msgmap);
						}

						count = msgmap.get(m);
						if (count == null) {
							count = new Integer(0);
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
	public void report(final Map<String, Map<String, IspMap<Map<String, Integer>>>> map) {
		System.out.println();
		map.forEach((country, ispmap) -> {
			int sum = ispmap.values().stream().mapToInt(addrmap -> {
				return addrmap.values().stream().mapToInt(msgmap -> {
					return msgmap.values().stream().mapToInt(c -> c.intValue()).sum();
				}).sum();
			}).sum();
			System.out.println(("".equals(country) ? "<MAGLIST>" : country) + " : " + sum);

			ispmap.forEach((isp, addrmap) -> {
				int sum2 = addrmap.values().stream().mapToInt(msgmap -> {
					return msgmap.values().stream().mapToInt(c -> c.intValue()).sum();
				}).sum();
				System.out.println(new StringBuilder().append("\t").append(isp).append(" : ").append(sum2));

				// 今のところ、IspMapにはforEach()を実装していないので、addrmap.forEach(...)は使用できない
				addrmap.keySet().forEach(addr -> {
					Map<String, Integer> msgmap = addrmap.get(addr);
					int sum3 = msgmap.values().stream().mapToInt(c -> c.intValue()).sum();
					System.out.println(new StringBuilder().append("\t\t").append(addr).append(" : ").append(sum3));

					msgmap.forEach((msg, count) -> {
						System.out.println(new StringBuilder().append("\t\t\t[ ").append(msg).append(" ] : ").append(count));
					});
				});
			});
			System.out.println();
		});
		System.out.println();
	}

	public static void main(String ... argv) {
		if (argv.length < 2) {
			System.err.println("usage: java logcheck.Checker4 knownlist maglist [accesslog...]");
			System.exit(1);
		}

		int rc = 0;
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker4 application = container.instance().select(Checker4.class).get();
			application.init(argv[0], argv[1]).start(argv, 2);
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
			rc = 1;
		}
		System.exit(rc);
	}
}
