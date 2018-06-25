package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import logcheck.annotations.UseChecker8;
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
@UseChecker8
public class Checker8 extends AbstractChecker<Map<String, Map<Isp, Map<NetAddr, Map<String, Map<String, AccessLogSummary>>>>>> {

	@Inject protected KnownList knownlist;
	@Inject protected MagList maglist;

	@Inject private transient Logger log;

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

		Pattern ptn = Pattern.compile("VPN Tunneling: Session started for user with IPv4 address [\\d\\.]+, hostname [\\w\\.-]+");
		if (ptn.matcher(b.getMsg()).matches()) {
			return ptn.toString();
		}
		if (!b.getMsg().contains("failed")) {
			// failed が含まれないメッセージは集約する
			return INFO_SUMMARY_MSG;
		}
		log.warning("(Pattern): \"" + b.getMsg() + "\"");
		return b.getMsg();
	}

	private void sub(Map<String, Map<Isp, Map<NetAddr, Map<String, Map<String, AccessLogSummary>>>>> map,
			IspList isp, AccessLogBean b, String pattern)
	{
		NetAddr addr = b.getAddr();

		Map<Isp, Map<NetAddr, Map<String, Map<String, AccessLogSummary>>>> ispmap;
		Map<NetAddr, Map<String, Map<String, AccessLogSummary>>> addrmap;
		Map<String, Map<String, AccessLogSummary>> idmap;
		Map<String, AccessLogSummary> msgmap;
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

		idmap = addrmap.get(addr);
		if (idmap == null) {
			idmap = new TreeMap<>();
			addrmap.put(addr, idmap);
		}

		msgmap = idmap.get(b.getId());
		if (msgmap == null) {
			msgmap = new TreeMap<>();
			idmap.put(b.getId(), msgmap);
		}

		msg = msgmap.get(pattern);
		if (msg == null) {
			msg = new AccessLogSummary(b, pattern);
			msgmap.put(pattern, msg);
		}
		else {
			msg.update(b);
		}
	}

	@Override
	public Map<String, Map<Isp, Map<NetAddr, Map<String, Map<String, AccessLogSummary>>>>> call(Stream<String> stream)
		{
		final Map<String, Map<Isp, Map<NetAddr, Map<String, Map<String, AccessLogSummary>>>>> map = new TreeMap<>();
		stream//.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.forEach(b -> {
					String pattern = getPattern(b);
					NetAddr addr = b.getAddr();
					IspList isp = maglist.get(addr);
					if (isp == null) {
						isp = knownlist.get(addr);
					}

					if (isp != null) {
						sub(map, isp, b, pattern);
					}
					else {
						addrErrs.add(b.getAddr());
					}
				});
		return map;
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
								out.println(new StringBuilder(country)
										.append("\t").append(isp.getName())
										.append("\t").append(addr)
										.append("\t").append(id)
										.append("\t").append(pattern)
										.append("\t").append(role)
										.append("\t").append(msg.getFirstDate())
										.append("\t").append(msg.getLastDate())
										.append("\t").append(msg.getCount())	//　rolesの出力数倍になる
										)
							)
						)
					)
				)
			)
		);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper<Checker8>(Checker8.class).weld(new AnnotationLiteral<UseChecker8>(){
			private static final long serialVersionUID = 1L;
		}, 2, argv);
		System.exit(rc);
	}
}
