package logcheck;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import javax.inject.Inject;

import logcheck.isp.Isp;
import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.log.AccessLogBean;
import logcheck.mag.MagList;
import logcheck.util.net.NetAddr;
import logcheck.util.weld.WeldWrapper;

/*
 * 利用申請外接続の検索処理：その2
 * Checker12のログ数の集約を行わないバージョン
 * 全てのログが出力されるので、認証失敗の状況などより細かい解析が可能
 */
public class Checker13 extends AbstractChecker<Map<String, Map<Isp, List<AccessLogBean>>>> {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	//private static final Pattern IP_RANGE_PATTERN = Pattern.compile("Testing Source IP realm restrictions failed for [\\S ]*/NSSDC-Auth\\d(\\(\\w+\\))? *");
	//private static final Pattern IP_RANGE_PATTERN = Pattern.compile("Testing Source IP realm restrictions failed for [\\S ]*/NSSDC-Auth\\d+(\\([\\w_]+\\))? *");

	public void init(String...argv) throws Exception {
		this.knownlist.load(argv[0]);
		this.maglist.load(argv[1]);
	}

	@Override
	public Map<String, Map<Isp, List<AccessLogBean>>> call(Stream<String> stream) throws Exception {
		final Map<String, Map<Isp, List<AccessLogBean>>> map = new TreeMap<>();
		stream.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
//				.filter(b -> IP_RANGE_PATTERN.matcher(b.getMsg()).matches())
				.filter(b -> Stream.of(IP_RANGE_PATTERN)
						// 正規化表現に一致するメッセージのみを処理対象にする
						.anyMatch(p -> p.matcher(b.getMsg()).matches())
						)
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
		map.forEach((country, ispmap) -> 
			ispmap.forEach((isp, addrmap) -> 
				addrmap.forEach(msg -> 
					out.println(new StringBuilder(country)
							.append("\t").append(isp.getName())
							.append("\t").append(msg.getAddr())
							.append("\t").append(msg.getDate())
							)
				)
			)
		);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper<Checker13>(Checker13.class).weld(2, argv);
		System.exit(rc);
	}
}
