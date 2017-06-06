package logcheck;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import logcheck.isp.Isp;
import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.log.AccessLogSummary;
import logcheck.mag.MagList;
import logcheck.util.net.NetAddr;

/*
 * 利用申請外接続の検索処理：
 * VPNログを読込、送信元IPアドレスが申請外のログ（IP_RANGE_PATTERN）に合致する場合は、そのログをコレクションに登録する。
 * もし、ログのIPアドレスが、コレクションのエントリに存在していた場合はログ数を更新する。
 */
public class Checker12 extends AbstractChecker<Map<String, Map<Isp, Map<NetAddr, AccessLogSummary>>>> {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	private Map<String, Map<Isp, Map<NetAddr, AccessLogSummary>>> map = new TreeMap<>();

	private static final Pattern IP_RANGE_PATTERN = Pattern.compile("Testing Source IP realm restrictions failed for /NSSDC-Auth1 *");

	public Checker12 init(String knownfile, String magfile) throws Exception {
		this.knownlist.load(knownfile);
		this.maglist.load(magfile);
		return this;
	}
/*
	public static boolean test(AccessLogBean b) {
		// メッセージにIPアドレスなどが含まれるログは、それ以外の部分を比較対象とするための前処理
		return IP_RANGE_PATTERN.matcher(b.getMsg()).matches();
	}
*/
	@Override
	public Map<String, Map<Isp, Map<NetAddr, AccessLogSummary>>> call(Stream<String> stream) throws Exception {
		stream.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
//				.filter(Checker12::test)
				.filter(b -> IP_RANGE_PATTERN.matcher(b.getMsg()).matches())
				.forEach(b -> {
					NetAddr addr = b.getAddr();
					IspList isp = maglist.get(addr);
					if (isp == null) {
						isp = knownlist.get(addr);
					}

					if (isp != null) {
						Map<Isp, Map<NetAddr, AccessLogSummary>> ispmap;
						Map<NetAddr, AccessLogSummary> addrmap;
						AccessLogSummary msg;

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

						msg = addrmap.get(addr);
						if (msg == null) {
							msg = new AccessLogSummary(b, IP_RANGE_PATTERN.toString());
							addrmap.put(addr, msg);
						}
						else {
							msg.update(b);
						}

					} else {
//						System.err.println("unknown ip: addr=" + addr);
						log.warning("unknown ip: addr=" + addr);
					}
				});
		return map;
	}

	@Override
	public void report() {
		System.out.println("国\tISP/プロジェクト\tアドレス\t初回日時\t最終日時\tログ数");
		map.forEach((country, ispmap) -> {
			ispmap.forEach((isp, addrmap) -> {
				addrmap.forEach((addr, msg) -> {
					System.out.println(
							new StringBuilder(country)
									.append("\t")
									.append(isp)
									.append("\t")
									.append(addr)
									.append("\t")
									.append(msg.getFirstDate())
									.append("\t")
									.append(msg.getLastDate())
									.append("\t")
									.append(msg.getCount()));
				});
			});
		});
	}

	public static void main(String... argv) {
		if (argv.length < 2) {
			System.err.println("usage: java logcheck.Checker12 knownlist maglist [accesslog...]");
			System.exit(1);
		}

		int rc = 0;
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker12 application = container.instance().select(Checker12.class).get();
			application.init(argv[0], argv[1]).start(argv, 2);
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
			rc = 1;
		}
		System.exit(rc);
	}
}
