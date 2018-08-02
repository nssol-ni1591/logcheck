package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
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
import logcheck.util.NetAddr;
import logcheck.util.WeldWrapper;

/*
 * 利用申請外接続の検索処理：その2
 * Checker12のログ数の集約を行わないバージョン
 * 全てのログが出力されるので、認証失敗の状況などより細かい解析が可能
 */
public class Checker13a extends AbstractChecker<Map<String, Map<Isp, List<AccessLogBean>>>> {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	public void init(String...argv) throws IOException, ClassNotFoundException, SQLException {
		this.knownlist.load(argv[0]);
		this.maglist.load(argv[1]);
	}

	@Override
	public Map<String, Map<Isp, List<AccessLogBean>>> call(Stream<String> stream) {
		return stream.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.filter(b -> Stream.of(IP_RANGE_PATTERN)
						// 正規化表現に一致するメッセージのみを処理対象にする
						.anyMatch(p -> p.matcher(b.getMsg()).matches())
						)
				.map(LogWrapper::new)
				.filter(log -> log.getIspList() != null)
				.collect(Collectors.groupingBy(
						LogWrapper::getCountry
						, TreeMap::new
						, Collectors.groupingBy(
								LogWrapper::getIspList
								, TreeMap::new
								, Collectors.mapping(LogWrapper::getAccessLogBean
										, Collectors.toList()))
						));
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
		int rc = new WeldWrapper(Checker13a.class).weld(2, argv);
		System.exit(rc);
	}

	class LogWrapper {

		private final AccessLogBean b;
		private final String country;
		private final IspList isp;

		LogWrapper(AccessLogBean b) {
			this.b = b;
			NetAddr addr = b.getAddr();
			this.isp = getIsp(addr, maglist, knownlist);
			this.country = isp.getCountry();
		}

		String getCountry() {
			return country;
		}
		IspList getIspList() {
			return isp;
		}
		AccessLogBean getAccessLogBean() {
			return b;
		}
	}

}
