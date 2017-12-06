package logcheck;

import java.io.PrintWriter;
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
import logcheck.mag.MagList;
import logcheck.util.net.NetAddr;
import logcheck.util.weld.WeldWrapper;

/*
 * ISP > IPアドレス > メッセージ毎にログ数を集計する
 */
public class Checker3 extends AbstractChecker<Map<String, IspMap<Map<String, Integer>>>> {

	@Inject private Logger log;

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	private static final Pattern[] FAIL_PATTERNS_ALL;
	static {
		FAIL_PATTERNS_ALL = new Pattern[FAIL_PATTERNS.length + FAIL_PATTERNS_DUP.length];
		System.arraycopy(FAIL_PATTERNS, 0, FAIL_PATTERNS_ALL, 0, FAIL_PATTERNS.length);
		System.arraycopy(FAIL_PATTERNS_DUP, 0, FAIL_PATTERNS_ALL, FAIL_PATTERNS.length, FAIL_PATTERNS_DUP.length);
	}

	public void init(String...argv) throws Exception {
		this.knownlist.load(argv[0]);
		this.maglist.load(argv[1]);
	}

	@Override
	public Map<String, IspMap<Map<String, Integer>>> call(Stream<String> stream) throws Exception {
		final Map<String, IspMap<Map<String, Integer>>> map = new TreeMap<>();
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

					NetAddr addr = b.getAddr();
					IspList isp = maglist.get(addr);
					if (isp == null) {
						isp = knownlist.get(addr);
					}

					if (isp != null) {
						IspMap<Map<String, Integer>> ispmap = map.get(isp.getName());
						if (ispmap == null) {
							ispmap = new IspMap<>(isp.getName(), isp.getCountry());
							map.put(isp.getName(), ispmap);
						}

						Map<String, Integer> client = ispmap.get(addr);
						if (client == null) {
							client = new TreeMap<>();
							ispmap.put(addr, client);
						}

						Integer count = client.get(m);
						if (count == null) {
							count = Integer.valueOf(0);
						}
						count += 1;
						client.put(m, count);
					}
					else {
						log.log(Level.WARNING, "unknown ip: addr={0}", addr);
					}
				});
		return map;
	}

	@Override
	public void report(final PrintWriter out, final Map<String, IspMap<Map<String, Integer>>> map) {
		map.values().forEach(isp -> {
			out.println();
			out.println(isp.getName() + (isp.getCountry() == null ? "" : " (" + isp.getCountry() + ")") + " : ");
			isp.keySet().forEach(addr -> {
				Map<String, Integer> msgs = isp.get(addr);
				out.println("\t" + addr + " : ");
				msgs.keySet().forEach(msg -> 
					out.println("\t\t[ " + msg + " ] : " + msgs.get(msg))
				);
			});
		});
		out.println();
	}

	public static void main(String ... argv) {
		int rc = new WeldWrapper<Checker3>(Checker3.class).weld(2, argv);
		System.exit(rc);
	}
}
