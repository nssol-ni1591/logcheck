package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
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

	public void init(String...argv) throws IOException, ClassNotFoundException, SQLException {
		this.knownlist.load(argv[0]);
		this.maglist.load(argv[1]);
	}

	@Override
	public Map<String, Map<Isp, List<AccessLogBean>>> call(Stream<String> stream) {
		final Map<String, Map<Isp, List<AccessLogBean>>> map = new TreeMap<>();
		stream.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.filter(b -> Stream.of(IP_RANGE_PATTERN)
						// 正規化表現に一致するメッセージのみを処理対象にする
						.anyMatch(p -> p.matcher(b.getMsg()).matches())
						)
				.forEach(b -> {
					NetAddr addr = b.getAddr();
					IspList isp = getIsp(addr, maglist, knownlist);
					if (isp != null) {
						Map<Isp, List<AccessLogBean>> ispmap;
						List<AccessLogBean> addrmap;

						ispmap = map.get(isp.getCountry());
						if (ispmap == null) {
							ispmap = new TreeMap<>();
							map.put(isp.getCountry(), ispmap);
						}

						addrmap = ispmap.computeIfAbsent(isp, key -> new ArrayList<>());

						addrmap.add(b);
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
					out.println(Stream.of(country
							, isp.getName()
							, msg.getAddr().toString()
							, msg.getDate()
							)
							.collect(Collectors.joining("\t")))
						))
				);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper<Checker13>(Checker13.class).weld(2, argv);
		System.exit(rc);
	}
}
