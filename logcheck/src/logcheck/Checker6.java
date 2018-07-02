package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
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
public class Checker6 extends AbstractChecker<Map<String, Map<IspList, Map<String, Map<NetAddr, Integer>>>>> {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	public void init(String...argv) throws IOException, ClassNotFoundException, SQLException {
		this.knownlist.load(argv[0]);
		this.maglist.load(argv[1]);
	}

	private void sub(Map<String, Map<IspList, Map<String, Map<NetAddr, Integer>>>> map,
			IspList isp, AccessLogBean b, String msg)
	{
		NetAddr addr = b.getAddr();

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

		addrmap = msgmap.get(msg);
		if (addrmap == null) {
			addrmap = new TreeMap<>();
			msgmap.put(msg, addrmap);
		}

		count = addrmap.get(addr);
		if (count == null) {
			count = Integer.valueOf(0);
		}
		count += 1;
		addrmap.put(addr, count);
	}

	@Override
	public Map<String, Map<IspList, Map<String, Map<NetAddr, Integer>>>> call(Stream<String> stream) {
		final Map<String, Map<IspList, Map<String, Map<NetAddr, Integer>>>> map = new TreeMap<>();
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
					IspList isp = getIsp(addr, maglist, knownlist);
					if (isp != null) {
						sub(map, isp, b, m);
					}
				});
		return map;
	}

	/*
	 * 国 > ISP > メッセージ > クライアントIP 毎に出力する
	 */
	@Override
	public void report(final PrintWriter out, final Map<String, Map<IspList, Map<String, Map<NetAddr, Integer>>>> map) {
		out.println();
		map.forEach((country, ispmap) -> {
			int sum = ispmap.values().stream().mapToInt(msgmap -> 
				msgmap.values().stream().mapToInt(addrmap -> 
					addrmap.values().stream().mapToInt(Integer::intValue).sum()
				).sum()
			).sum();
			int sum1 = ispmap.values().stream().mapToInt(msgmap -> 
				msgmap.get(INFO_SUMMARY_MSG) == null ? 0 : msgmap.get(INFO_SUMMARY_MSG).values().stream().mapToInt(Integer::intValue).sum()
			).sum();
			out.println(("".equals(country) ? "<MAGLIST>" : country) +
				new StringBuilder().append(" : ").append(sum - sum1).append(" / ").append(sum).append(" => ").append((sum - sum1) * 100 / sum).append("%").toString());

			ispmap.forEach((isp, msgmap) -> {
				int sum2 = msgmap.values().stream().mapToInt(addrmap -> 
					addrmap.values().stream().mapToInt(Integer::intValue).sum()
				).sum();
				int sum21 = msgmap.get(INFO_SUMMARY_MSG) == null ? 0 : msgmap.get(INFO_SUMMARY_MSG).values().stream().mapToInt(Integer::intValue).sum();
				out.println(new StringBuilder().append("\t").append(isp.getName()).append(" : ").append(sum2 - sum21).append(" / ").append(sum2).append(" => ").append((sum2 - sum21) * 100 / sum2).append("%"));

				msgmap.forEach((msg, addrmap) -> {
					int sum3 = addrmap.values().stream().mapToInt(Integer::intValue).sum();
					out.println(new StringBuilder().append("\t\t[ ").append(msg).append(" ] : ").append(sum3));

					addrmap.forEach((addr, count) -> 
						out.println(new StringBuilder().append("\t\t\t").append(addr).append(" : ").append(count))
					);
				});
			});
		});
		out.println();
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper<Checker6>(Checker6.class).weld(2, argv);
		System.exit(rc);
	}
}
