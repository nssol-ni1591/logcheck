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
import logcheck.isp.IspMap;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.log.AccessLogBean;
import logcheck.mag.MagList;
import logcheck.util.NetAddr;
import logcheck.util.WeldWrapper;

/*
 * 国 > ISP > IPアドレス > メッセージ毎にログ数を集計する
 */
public class Checker4 extends AbstractChecker<Map<String ,Map<String, IspMap<Map<String, Integer>>>>> {

	@Inject private Logger log;

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	public void init(String...argv) throws IOException, ClassNotFoundException, SQLException {
		this.knownlist.load(argv[0]);
		this.maglist.load(argv[1]);
	}

	private void sub(Map<String, Map<String, IspMap<Map<String, Integer>>>> map,
			IspList isp, AccessLogBean b, String m)
	{
		NetAddr addr = b.getAddr();

		Map<String, IspMap<Map<String, Integer>>> ispmap;
		IspMap<Map<String, Integer>> addrmap;
		Map<String, Integer> msgmap;
		Integer count;

		ispmap = map.get(isp.getCountry());
		if (ispmap == null) {
			ispmap = new TreeMap<>();
			map.put(isp.getCountry(), ispmap);
		}
		//ispmap = map.computeIfAbsent(isp.getCountry(), key -> new TreeMap<>())
		/*
		ispmap = map.computeIfAbsent(isp.getCountry(), new Function<String, Map<String, IspMap<Map<String, Integer>>>>() {-
			@Override
			public Map<String, IspMap<Map<String, Integer>>> apply(String t) {-
				return new TreeMap<>();-
			}-
		});-
		*/

		addrmap = ispmap.computeIfAbsent(isp.getName(), key -> new IspMap<>(isp.getName(), isp.getCountry()));

		msgmap = addrmap.computeIfAbsent(addr, key -> new TreeMap<>());

		count = msgmap.computeIfAbsent(m, key -> Integer.valueOf(0));
		count += 1;
		msgmap.put(m, count);
	}

	/*
	 * 国 > ISP > IPアドレス > メッセージ毎に出力する
	 */
	@Override
	public Map<String, Map<String, IspMap<Map<String, Integer>>>> call(Stream<String> stream) {
		final Map<String, Map<String, IspMap<Map<String, Integer>>>> map = new TreeMap<>();
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
						log.log(Level.WARNING, "msg={0}", b);
					}

					NetAddr addr = b.getAddr();
					IspList isp = getIsp(addr, maglist, knownlist);
					if (isp != null) {
						sub(map, isp, b, m);
					}
				});
		return map;
	}

	@Override
	public void report(final PrintWriter out, final Map<String, Map<String, IspMap<Map<String, Integer>>>> map) {
		out.println();
		map.forEach((country, ispmap) -> {
			int sum = ispmap.values().stream().mapToInt(addrmap ->
				addrmap.values().stream().mapToInt(msgmap -> 
					msgmap.values().stream().mapToInt(Integer::intValue).sum()
				).sum()
			).sum();
			out.println(("".equals(country) ? "<MAGLIST>" : country) + " : " + sum);

			ispmap.forEach((isp, addrmap) -> {
				int sum2 = addrmap.values().stream().mapToInt(msgmap -> 
					msgmap.values().stream().mapToInt(Integer::intValue).sum()
				).sum();
				out.println(new StringBuilder().append("\t").append(isp).append(" : ").append(sum2));

				// 今のところ、IspMapにはforEach()を実装していないので、addrmap.forEach(...)は使用できない
				addrmap.keySet().forEach(addr -> {
					Map<String, Integer> msgmap = addrmap.get(addr);
					int sum3 = msgmap.values().stream().mapToInt(Integer::intValue).sum();
					out.println(new StringBuilder().append("\t\t").append(addr).append(" : ").append(sum3));

					msgmap.forEach((msg, count) -> 
						out.println(new StringBuilder().append("\t\t\t[ ").append(msg).append(" ] : ").append(count))
					);
				});
			});
			out.println();
		});
		out.println();
	}

	public static void main(String ... argv) {
		int rc = new WeldWrapper(Checker4.class).weld(2, argv);
		System.exit(rc);
	}
}
