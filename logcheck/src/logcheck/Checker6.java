package logcheck;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.mag.MagList;
import logcheck.util.NetAddr;

/*
 * 国 > ISP > メッセージ > クライアントIP 毎にログ数を集計する
 */
public class Checker6 extends AbstractChecker<Map<String, Map<IspList, Map<String, Map<NetAddr, Integer>>>>> {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	private static final String INFO_SUMMARY_MSG = "<><><> Information message summary <><><>";

	public Checker6 init(String knownfile, String magfile) throws Exception {
		this.knownlist.load(knownfile);
		this.maglist.load(magfile);
		return this;
	}

	public Map<String, Map<IspList, Map<String, Map<NetAddr, Integer>>>> call(Stream<String> stream) throws Exception {
		Map<String, Map<IspList, Map<String, Map<NetAddr, Integer>>>> map = new TreeMap<>();
		stream.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.forEach(b -> {
					// メッセージにIPアドレスなどが含まれるログは、それ以外の部分を比較対象とするための前処理
					Optional<String> rc = Stream.of(FAIL_PATTERNS)
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
							count = new Integer(0);
						}
						count += 1;
						addrmap.put(addr, count);
					} else {
						System.err.println("unknown ip: addr=" + addr);
					}
				});
		return map;
	}

	public void report(Map<String, Map<IspList, Map<String, Map<NetAddr, Integer>>>> map) {
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
				System.out.println(new StringBuilder().append("\t").append(isp).append(" : ").append(sum2 - sum21).append(" / ").append(sum2).append(" => ").append((sum2 - sum21) * 100 / sum2).append("%"));

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
			Checker6 application = container.instance().select(Checker6.class).get();
			application.init(argv[0], argv[1]).start(argv, 2);
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
			rc = 1;
		}
		System.exit(rc);
	}
}
