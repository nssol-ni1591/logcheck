package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.inject.Inject;

import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.mag.MagList;
import logcheck.util.NetAddr;
import logcheck.util.WeldWrapper;

/*
 * 国 > ISP > メッセージ > クライアントIP 毎にログ数を集計する
 */
public class Checker5 extends AbstractChecker<Map<String, Map<IspList, Map<String, Integer>>>> {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	public void init(String...argv) throws IOException, ClassNotFoundException, SQLException {
		this.knownlist.load(argv[0]);
		this.maglist.load(argv[1]);
	}

	private void sub(Map<String, Map<IspList, Map<String, Integer>>> map,
			IspList isp, String msg)
	{
		Map<IspList, Map<String, Integer>> ispmap = map.get(isp.getCountry());
		if (ispmap == null) {
			ispmap = new TreeMap<>();
			map.put(isp.getCountry(), ispmap);
		}

		Map<String, Integer> msgmap = ispmap.computeIfAbsent(isp, key -> new TreeMap<>());

		Integer count = msgmap.computeIfAbsent(msg, key -> Integer.valueOf(0));
		count += 1;
		msgmap.put(msg, count);		
	}

	@Override
	public Map<String, Map<IspList, Map<String, Integer>>> call(Stream<String> stream) {
		final Map<String, Map<IspList, Map<String, Integer>>> map = new TreeMap<>();
		stream.parallel()
				//.filter(AccessLog::test)
				.map(AccessLog::parse)
				.filter(Objects::nonNull)
				.forEach(b -> {
					// ログのメッセージ部分はPatternの正規化表現で集約するため、対象ログが一致したPattern文字列を取得する
					Optional<String> rc = Stream.of(FAIL_PATTERNS_ALL)
							.filter(p -> p.matcher(b.getMsg()).matches())
							.map(Pattern::toString)
							.findFirst();
					String msg = rc.isPresent() ? rc.get() : b.getMsg();
					if (!rc.isPresent()) {
						msg = INFO_SUMMARY_MSG;
					}

					NetAddr addr = b.getAddr();
					IspList isp = getIsp(addr, maglist, knownlist);
					if (isp != null) {
						sub(map, isp, msg);
					}
				});
		return map;
	}

	/*
	 * 国 > ISP > メッセージ > クライアントIP 毎に出力する
	 */
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
		int rc = new WeldWrapper(Checker5.class).weld(2, argv);
		System.exit(rc);
	}
}
