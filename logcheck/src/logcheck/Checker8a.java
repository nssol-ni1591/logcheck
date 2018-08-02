package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;
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
import logcheck.util.net.NetAddr;
import logcheck.util.weld.WeldWrapper;

/*
 * ログ解析用の集約ツール1：
 * 国 > ISP > クライアントIP > メッセージ  > ID 毎にログ数を集計する。
 * 利用方法としては、プログラムの出力を直接参照するのではなく、Excelに読み込ませpivotで解析する想定のためTSV形式で出力する。
 * なお、このツールでは、正常系ログは集約を行う。
 */
public class Checker8a extends AbstractChecker<Map<String, Map<Isp, Map<NetAddr, Map<String, Map<String, AccessLogSummary>>>>>> {

	@Inject protected KnownList knownlist;
	@Inject protected MagList maglist;

	public void init(String...argv) throws IOException, ClassNotFoundException, SQLException {
		this.knownlist.load(argv[0]);
		this.maglist.load(argv[1]);
	}

	// ログのメッセージ部分はPatternの正規化表現で集約するため、対象ログと一致したPattern文字列を取得する
	protected String getPattern(AccessLogBean b) {
		Optional<String> rc = Stream.of(FAIL_PATTERNS)
				.filter(p -> p.matcher(b.getMsg()).matches())
				.map(Pattern::toString)
				.findFirst();
		if (rc.isPresent()) {
			return rc.get();
		}

		rc = Stream.of(FAIL_PATTERNS_DUP)
				.filter(p -> p.matcher(b.getMsg()).matches())
				.map(Pattern::toString)
				.findFirst();
		if (rc.isPresent()) {
			//同一原因で複数出力されるログは識別のため"（）"を付加する
			return "(" + rc.get() + ")";
		}
		
		// セッション開始メッセージは集計対象にする
		Pattern ptn = SESS_START_PATTERN;
		if (ptn.matcher(b.getMsg()).matches()) {
			return ptn.toString();
		}
		if (!b.getMsg().contains("failed")) {
			// failed が含まれないメッセージは集約する
			return INFO_SUMMARY_MSG;
		}
		ptnErrs.add(b.getMsg());
		return b.getMsg();
	}

	@Override
	public Map<String, Map<Isp, Map<NetAddr, Map<String, Map<String, AccessLogSummary>>>>> call(Stream<String> stream)
	{
		return stream//.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.map(LogWrapper::new)
				.filter(log -> Objects.nonNull(log.getIspList()))
				.collect(Collectors.groupingBy(
						LogWrapper::getCountry
						, TreeMap::new
						, Collectors.groupingBy(
								LogWrapper::getIspList
								, TreeMap::new
								, Collectors.groupingBy(
										LogWrapper::getAddr
										, TreeMap::new
										, Collectors.groupingBy(
												LogWrapper::getId
												, TreeMap::new
												, Collectors.toMap(
														LogWrapper::getMsg
														, LogWrapper::getAccessLogSummary
														, (t, u) -> {
															t.update(u);
															return t;
														}
														))))));
	}

	@Override
	public void report(final PrintWriter out, 
			final Map<String, Map<Isp, Map<NetAddr, Map<String, Map<String, AccessLogSummary>>>>> map)
	{
		out.println("国\tISP/プロジェクト\tアドレス\tユーザID\tメッセージ\tロール\t初回日時\t最終日時\tログ数");
		map.forEach((country, ispmap) -> 
			ispmap.forEach((isp, addrmap) -> 
				addrmap.forEach((addr, idmap) -> 
					idmap.forEach((id, msgmap) -> 
						msgmap.forEach((pattern, msg) -> 
							Stream.of(msg.getRoles()).forEach(role -> 
								out.println(Stream.of(country
										, isp.getName()
										, addr.toString()
										, id
										, pattern
										, role
										, msg.getFirstDate()
										, msg.getLastDate()
										, String.valueOf(msg.getCount())	//　rolesの出力数倍になる
										)
										.collect(Collectors.joining("\t")))
									)
						)
					)
				)
			)
		);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper(Checker8.class).weld(2, argv);
		System.exit(rc);
	}
	
	class LogWrapper {

		private final String country;
		private final IspList isp;
		private final NetAddr addr;
		private final String id;
		private final String msg;
		private final AccessLogSummary sum;

		LogWrapper(AccessLogBean b) {
			this.addr = b.getAddr();
			this.isp = getIsp(addr, maglist, knownlist);
			this.country = isp.getCountry();
			this.id = b.getId();
			this.msg = getPattern(b);
			this.sum = new AccessLogSummary(b, msg);
		}

		public String getCountry() {
			return country;
		}
		public IspList getIspList() {
			return isp;
		}
		public NetAddr getAddr() {
			return addr;
		}
		public String getId() {
			return id;
		}
		public String getMsg() {
			return msg;
		}
		public AccessLogSummary getAccessLogSummary() {
			return sum;
		}
	}
}
