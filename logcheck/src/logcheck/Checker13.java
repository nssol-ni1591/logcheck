package logcheck;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
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
import logcheck.log.AccessLogBean;
import logcheck.mag.MagList;
import logcheck.util.net.NetAddr;

/*
 * 利用申請外接続の検索処理：その2
 * Checker12のログ数の集約を行わないバージョン
 * 全てのログが出力されるので、認証失敗の状況などより細かい解析が可能
 */
public class Checker13 extends AbstractChecker<Map<String, Map<Isp, List<AccessLogBean>>>> {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

//	private final Map<String, Map<Isp, List<AccessLogBean>>> map = new TreeMap<>();
// for 2017-11-18
//	private static final Pattern IP_RANGE_PATTERN = Pattern.compile("Testing Source IP realm restrictions failed for /NSSDC-Auth1 *");
	private static final Pattern IP_RANGE_PATTERN = Pattern.compile("Testing Source IP realm restrictions failed for [\\S ]*/NSSDC-Auth\\d(\\(\\w+\\))? *");

	public Checker13 init(String knownfile, String magfile) throws Exception {
		this.knownlist.load(knownfile);
		this.maglist.load(magfile);
		return this;
	}

	@Override
	public Map<String, Map<Isp, List<AccessLogBean>>> call(Stream<String> stream) throws Exception {
		final Map<String, Map<Isp, List<AccessLogBean>>> map = new TreeMap<>();
		stream.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.filter(b -> IP_RANGE_PATTERN.matcher(b.getMsg()).matches())
				.forEach(b -> {
					NetAddr addr = b.getAddr();
					IspList isp = maglist.get(addr);
					if (isp == null) {
						isp = knownlist.get(addr);
					}

					if (isp != null) {
						Map<Isp, List<AccessLogBean>> ispmap;
						List<AccessLogBean> addrmap;

						ispmap = map.get(isp.getCountry());
						if (ispmap == null) {
							ispmap = new TreeMap<>();
							map.put(isp.getCountry(), ispmap);
						}

						addrmap = ispmap.get(isp);
						if (addrmap == null) {
							addrmap = new ArrayList<>();
							ispmap.put(isp, addrmap);
						}

						addrmap.add(b);
					}
					else {
						addrErrs.add(b.getAddr());
					}
				});
		return map;
	}

	@Override
	public void report(final PrintWriter out, final Map<String, Map<Isp, List<AccessLogBean>>> map) {
		out.println("国\tISP/プロジェクト\tアドレス\t日時");
		map.forEach((country, ispmap) -> {
			ispmap.forEach((isp, addrmap) -> {
				addrmap.forEach((msg) -> {
					out.println(new StringBuilder(country)
							.append("\t").append(isp.getName())
							.append("\t").append(msg.getAddr())
							.append("\t").append(msg.getDate())
							);
				});
			});
		});
	}

	public static void main(String... argv) {
		if (argv.length < 2) {
			System.err.println("usage: java logcheck.Checker13 knownlist maglist [accesslog...]");
			System.exit(1);
		}

		int rc = 0;
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker13 application = container.select(Checker13.class).get();
			application.init(argv[0], argv[1]).start(argv, 2);
		}
		catch (Exception ex) {
			rc = 1;
		}
		System.exit(rc);
	}
}
