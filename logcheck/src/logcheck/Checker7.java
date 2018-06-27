package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.log.AccessLogBean;
import logcheck.log.AccessLogSummary;
import logcheck.mag.MagList;
import logcheck.util.net.NetAddr;
import logcheck.util.weld.WeldWrapper;

/*
 * 国 > ISP > クライアントIP > メッセージ 毎にログ数を集計する
 * ⇒ MsgBean, Integerでは、日時を正確に処理できない
 */
public class Checker7 extends AbstractChecker<Map<String, Map<IspList, Map<NetAddr, Map<AccessLogSummary, Integer>>>>> {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	public void init(String...argv) throws IOException, ClassNotFoundException, SQLException {
		this.knownlist.load(argv[0]);
		this.maglist.load(argv[1]);
	}

	private void sub(Map<String, Map<IspList, Map<NetAddr, Map<AccessLogSummary, Integer>>>> map,
			IspList isp, AccessLogBean b, String pattern)
	{
		NetAddr addr = b.getAddr();

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
			count = Integer.valueOf(0);
		}
		else {
			msg.update(b);
		}
		count += 1;
		msgmap.put(msg, count);
	}

	@Override
	public Map<String, Map<IspList, Map<NetAddr, Map<AccessLogSummary, Integer>>>> call(Stream<String> stream) {
		final Map<String, Map<IspList, Map<NetAddr, Map<AccessLogSummary, Integer>>>> map = new TreeMap<>();
		stream.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.forEach(b -> {
					// ログのメッセージ部分はPatternの正規化表現で集約するため、対象ログが一致したPattern文字列を取得する
					Optional<String> rc = Stream.of(FAIL_PATTERNS_ALL)
							.filter(p -> p.matcher(b.getMsg()).matches())
							.map(Pattern::toString)
							.findFirst();
					String pattern = rc.isPresent() ? rc.get() : b.getMsg();
					if (!rc.isPresent()) {
						pattern = INFO_SUMMARY_MSG;
					}

					NetAddr addr = b.getAddr();
					IspList isp = getIsp(addr, maglist, knownlist);
					if (isp != null) {
						sub(map, isp, b, pattern);
					}
				});
		return map;
	}

	/*
	 * 国 > ISP > クライアントIP > メッセージ 毎に出力する
	 */
	@Override
	public void report(final PrintWriter out, 
			final Map<String, Map<IspList, Map<NetAddr, Map<AccessLogSummary, Integer>>>> map)
	{
		out.println("国\tISP/プロジェクト\tアドレス\tメッセージ\t初回日時\t最終日時\tログ数\tISP合計");
		map.forEach((country, ispmap) -> 

			ispmap.forEach((isp, addrmap) -> {
				int sumIspLog = addrmap.values().stream().mapToInt(msgmap -> 
					msgmap.values().stream().mapToInt(Integer::intValue).sum()
				).sum();

				addrmap.forEach((addr, msgmap) -> 
					msgmap.forEach((msg, count) -> 
						out.println(Stream.of(country.isEmpty() ? "<MAGLIST>" : country
								, isp.getName()
								, addr.toString()
								, msg.getPattern()
								, msg.getFirstDate()
								, msg.getLastDate()
								, String.valueOf(count)
								, String.valueOf(sumIspLog)
								)
								.collect(Collectors.joining("\t")))
							)
						);
			})
		);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper<Checker7>(Checker7.class).weld(2, argv);
		System.exit(rc);
	}
}
