package logcheck;

import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.inject.Inject;

import logcheck.isp.Isp;
import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.log.AccessLogSummary;
import logcheck.mag.MagList;
import logcheck.util.net.NetAddr;
import logcheck.util.weld.WeldWrapper;

/*
 * 利用申請外接続の検索処理：
 * VPNログを読込、送信元IPアドレスが申請外のログ（IP_RANGE_PATTERN）に合致する場合は、そのログをコレクションに登録する。
 * もし、ログのIPアドレスが、コレクションのエントリに存在していた場合はログ数を更新する。
 */
public class Checker12 extends AbstractChecker<Map<String, Map<Isp, Map<NetAddr, AccessLogSummary>>>> {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	private static final Pattern IP_RANGE_PATTERN = Pattern.compile("Testing Source IP realm restrictions failed for [\\S ]*/NSSDC-Auth\\d(\\(\\w+\\))? *");

	public void init(String...argv) throws Exception {
		this.knownlist.load(argv[0]);
		this.maglist.load(argv[1]);
	}

	@Override
	public Map<String, Map<Isp, Map<NetAddr, AccessLogSummary>>> call(Stream<String> stream) throws Exception {
		final Map<String, Map<Isp, Map<NetAddr, AccessLogSummary>>> map = new TreeMap<>();
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

					}
					else {
						addrErrs.add(b.getAddr());
					}
				});
		return map;
	}

	@Override
	public void report(final PrintWriter out, final Map<String, Map<Isp, Map<NetAddr, AccessLogSummary>>> map) {
		out.println("国\tISP/プロジェクト\tアドレス\t初回日時\t最終日時\tログ数");
		map.forEach((country, ispmap) -> 
			ispmap.forEach((isp, addrmap) -> 
				addrmap.forEach((addr, msg) -> 
					out.println(new StringBuilder(country)
							.append("\t").append(isp.getName())
							.append("\t").append(addr)
							.append("\t").append(msg.getFirstDate())
							.append("\t").append(msg.getLastDate())
							.append("\t").append(msg.getCount())
							)
				)
			)
		);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper<Checker12>(Checker12.class).weld(2, argv);
		System.exit(rc);
	}
}
