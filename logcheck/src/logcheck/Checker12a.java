package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import logcheck.isp.Isp;
import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.log.AccessLogBean;
import logcheck.log.AccessLogSummary;
import logcheck.mag.MagList;
import logcheck.util.NetAddr;
import logcheck.util.WeldWrapper;

/*
 * 利用申請外接続の検索処理：
 * VPNログを読込、送信元IPアドレスが申請外のログ（IP_RANGE_PATTERN）に合致する場合は、そのログをコレクションに登録する。
 * もし、ログのIPアドレスが、コレクションのエントリに存在していた場合はログ数を更新する。
 */
public class Checker12a extends AbstractChecker<Map<String, Map<Isp, Map<NetAddr, AccessLogSummary>>>> {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	public void init(String...argv) throws IOException, ClassNotFoundException, SQLException {
		this.knownlist.load(argv[0]);
		this.maglist.load(argv[1]);
	}

	@Override
	public Map<String, Map<Isp, Map<NetAddr, AccessLogSummary>>> call(Stream<String> stream) {
		return stream.parallel()
				//.filter(AccessLog::test)
				.map(AccessLog::parse)
				.filter(Objects::nonNull)
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
								, Collectors.toMap(
										LogWrapper::getAddr
										, LogWrapper::getSummary
										, (t, u) -> {
											t.update(u);
											return t;
										}
										, TreeMap::new		// 指定しないとHashMap
										)
								)
						));
	}

	@Override
	public void report(final PrintWriter out, final Map<String, Map<Isp, Map<NetAddr, AccessLogSummary>>> map) {
		out.println("国\tISP/プロジェクト\tアドレス\t初回日時\t最終日時\tログ数");
		map.forEach((country, ispmap) -> 
			ispmap.forEach((isp, addrmap) -> 
				addrmap.forEach((addr, msg) -> 
					out.println(Stream.of(country
							, isp.getName()
							, addr.toString()
							, msg.getFirstDate()
							, msg.getLastDate()
							, String.valueOf(msg.getCount())
							)
							.collect(Collectors.joining("\t")))
						)
					)
				);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper(Checker12a.class).weld(2, argv);
		System.exit(rc);
	}

	class LogWrapper {

		private final AccessLogBean b;
		private final IspList isp;
		private final NetAddr addr;
		private final AccessLogSummary summary;

		LogWrapper(AccessLogBean b) {
			this.b = b;
			this.addr = b.getAddr();
			this.isp = getIsp(addr, maglist, knownlist);
			this.summary = new AccessLogSummary(b, IP_RANGE_PATTERN.toString());
		}

		String getCountry() {
			return isp.getCountry();
		}
		NetAddr getAddr() {
			return addr;
		}
		IspList getIspList() {
			return isp;
		}
		AccessLogSummary getSummary() {
			return summary;
		}
		AccessLogBean getAccessLogBean() {
			return b;
		}
	}
}
