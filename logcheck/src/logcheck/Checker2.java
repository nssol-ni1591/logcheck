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
import logcheck.msg.MsgMap;
import logcheck.msg.MsgMapIsp;
import logcheck.util.NetAddr;

/*
 * 同一メッセージのログを、ISP別にクライアントのISPアドレス毎にログ数を集計する
 */
public class Checker2 extends AbstractChecker<Map<String, MsgMap>> {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	private Checker2() { }

	public Checker2 init(String knownfile, String magfile) throws Exception {
		this.knownlist.load(knownfile);
		this.maglist.load(magfile);
		return this;
	}

	public Map<String, MsgMap> call(Stream<String> stream) throws Exception {
		Map<String, MsgMap> map = new TreeMap<>();
		stream.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.forEach(b -> {
					// メッセージにIPアドレスなどが含まれるログは、それ以外の部分を比較対象とするための前処理
					Pattern[] patterns = new Pattern[INFO_PATTERNS.length + FAIL_PATTERNS.length];
					System.arraycopy(INFO_PATTERNS, 0, patterns, 0, INFO_PATTERNS.length);
					System.arraycopy(FAIL_PATTERNS, 0, patterns, INFO_PATTERNS.length, FAIL_PATTERNS.length);
					Optional<String> rc = Stream.of(patterns)
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

					MsgMap msg = map.get(m);
					if (msg == null) {
						msg = new MsgMap(m);
						map.put(m, msg);
					}

					if (isp != null) {
						MsgMapIsp msgisp = msg.get(isp.getName());
						if (msgisp == null) {
							msgisp = new MsgMapIsp(isp.getName(), isp);
							msg.put(isp.getName(), msgisp);
						}
						msgisp.addAddress(addr);		// ソースIPを登録する
					}
					else {
						System.err.println("unknown ip: addr=" + addr);
					}
				});
		return map;
	}

	public void report(Map<String, MsgMap> map) {
		map.values().forEach(msg -> {
			System.out.println();
			System.out.println("[ " + msg.getMsg() + " ] : " + msg.sum());
			msg.values().forEach(isp -> {
				System.out.println("\t" + isp.getIsp().getName() + ("".equals(isp.getIsp().getCountry()) ? "" : " (" + isp.getIsp().getCountry() + ")") + " : " + isp.getSum());
				isp.getAddress().forEach(addr -> System.out.println("\t\t" + addr + "=" + isp.getCount(addr)));
			});
		});
		System.out.println();
	}

	public static void main(String ... argv) {
		if (argv.length < 2) {
			System.err.println("usage: java logcheck.Checker knownlist maglist [accesslog...]");
			System.exit(1);
		}
		/*
		try {
			new Checker2(argv[0], argv[1]).start(argv, 2);
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
		*/
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker2 application = container.instance().select(Checker2.class).get();
			application.init(argv[0], argv[1]).start(argv, 2);
			System.exit(0);
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
		System.exit(1);
	}
}
