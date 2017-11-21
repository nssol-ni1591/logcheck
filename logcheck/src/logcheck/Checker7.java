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
import logcheck.log.AccessLogSummary;
import logcheck.mag.MagList;
import logcheck.util.net.NetAddr;

/*
 * 国 > ISP > クライアントIP > メッセージ 毎にログ数を集計する
 * ⇒ MsgBean, Integerでは、日時を正確に処理できない
 */
public class Checker7 extends AbstractChecker<Map<String, Map<IspList, Map<NetAddr, Map<AccessLogSummary, Integer>>>>> {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	private static final Pattern[] FAIL_PATTERNS_ALL;
	static {
		FAIL_PATTERNS_ALL = new Pattern[FAIL_PATTERNS.length + FAIL_PATTERNS_DUP.length];
		System.arraycopy(FAIL_PATTERNS, 0, FAIL_PATTERNS_ALL, 0, FAIL_PATTERNS.length);
		System.arraycopy(FAIL_PATTERNS_DUP, 0, FAIL_PATTERNS_ALL, FAIL_PATTERNS.length, FAIL_PATTERNS_DUP.length);
	}

	public Checker7 init(String knownfile, String magfile) throws Exception {
		this.knownlist.load(knownfile);
		this.maglist.load(magfile);
		return this;
	}

	@Override
	public Map<String, Map<IspList, Map<NetAddr, Map<AccessLogSummary, Integer>>>> call(Stream<String> stream)
			throws Exception {
		final Map<String, Map<IspList, Map<NetAddr, Map<AccessLogSummary, Integer>>>> map = new TreeMap<>();
		stream.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.forEach(b -> {
					// ログのメッセージ部分はPatternの正規化表現で集約するため、対象ログが一致したPattern文字列を取得する
					Optional<String> rc = Stream.of(FAIL_PATTERNS_ALL)
							.filter(p -> p.matcher(b.getMsg()).matches())
							.map(p -> p.toString())
							.findFirst();
					String pattern = rc.isPresent() ? rc.get() : b.getMsg();
					if (!rc.isPresent()) {
						pattern = INFO_SUMMARY_MSG;
					}

					NetAddr addr = b.getAddr();
					IspList isp = maglist.get(addr);
					if (isp == null) {
						isp = knownlist.get(addr);
					}

					if (isp != null) {
						Map<IspList, Map<NetAddr, Map<AccessLogSummary, Integer>>> ispmap;
						Map<NetAddr, Map<AccessLogSummary, Integer>> addrmap;
						Map<AccessLogSummary, Integer> msgmap;
						Integer count;

						ispmap = map.get(isp.getCountry());
						if (ispmap == null) {
							ispmap = new TreeMap<>();
							map.put(isp.getCountry(), ispmap);
						}

						addrmap = ispmap.get(isp);
						if (addrmap == null) {
							addrmap = new TreeMap<>();
							ispmap.put(isp, addrmap);
						}

						msgmap = addrmap.get(addr);
						if (msgmap == null) {
							msgmap = new TreeMap<>();
							addrmap.put(addr, msgmap);
						}

						AccessLogSummary msg = new AccessLogSummary(b, pattern);
						count = msgmap.get(msg);
						if (count == null) {
							count = new Integer(0);
						}
						else {
							msg.update(b);
						}
						count += 1;
						msgmap.put(msg, count);
					} else {
						System.err.println("unknown ip: addr=" + addr);
					}
				});
		return map;
	}

	@Override
	public void report(final PrintWriter out, 
			final Map<String, Map<IspList, Map<NetAddr, Map<AccessLogSummary, Integer>>>> map)
	{
		System.out.println("国\tISP/プロジェクト\tアドレス\tメッセージ\t初回日時\t最終日時\tログ数\tISP合計");
		map.forEach((country, ispmap) -> {

			ispmap.forEach((isp, addrmap) -> {
				int sumIspLog = addrmap.values().stream().mapToInt(msgmap -> {
					return msgmap.values().stream().mapToInt(c -> c.intValue()).sum();
				}).sum();

				addrmap.forEach((addr, msgmap) -> {

					msgmap.forEach((msg, count) -> {
						System.out.println(new StringBuilder("".equals(country) ? "<MAGLIST>" : country)
								.append("\t").append(isp.getName())
								.append("\t").append(addr)
								.append("\t").append(msg.getPattern())
								.append("\t").append(msg.getFirstDate())
								.append("\t").append(msg.getLastDate())
								.append("\t").append(count)
								.append("\t").append(sumIspLog)
								);
					});
				});
			});
		});
	}

	public static void main(String... argv) {
		if (argv.length < 2) {
			System.err.println("usage: java logcheck.Checker4 knownlist maglist [accesslog...]");
			System.exit(1);
		}

		int rc = 0;
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker7 application = container.select(Checker7.class).get();
			application.init(argv[0], argv[1]).start(argv, 2);
		}
		catch (Exception ex) {
			rc = 1;
		}
		System.exit(rc);
	}
}
