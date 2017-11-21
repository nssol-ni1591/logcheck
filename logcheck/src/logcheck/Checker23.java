package logcheck;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import logcheck.annotations.UseChecker23;
import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.log.AccessLogBean;
import logcheck.log.AccessLogSummary;
import logcheck.mag.MagList;
import logcheck.util.net.NetAddr;

/*
 * ログ解析用の集約ツール1：
 * 国 > ISP > クライアントIP > メッセージ  > ID 毎にログ数を集計する。
 * 利用方法としては、プログラムの出力を直接参照するのではなく、Excelに読み込ませpivotで解析する想定のためTSV形式で出力する。
 * なお、このツールでは、正常系ログは集約を行う。
 */
@UseChecker23
public class Checker23 extends AbstractChecker<List<AccessLogSummary>> {

	@Inject protected KnownList knownlist;
	@Inject protected MagList maglist;

	@Inject private Logger log;

	public Checker23 init(String knownfile, String magfile) throws Exception {
		this.knownlist.load(knownfile);
		this.maglist.load(magfile);
		return this;
	}

	// ログのメッセージ部分はPatternの正規化表現で集約するため、対象ログと一致したPattern文字列を取得する
	protected String getPattern(AccessLogBean b) {
		Optional<String> rc = Stream.of(FAIL_PATTERNS)
				.filter(p -> p.matcher(b.getMsg()).matches())
				.map(p -> p.toString())
				.findFirst();
		if (rc.isPresent()) {
			return rc.get();
		}
		// 同一原因エラーログは集約する
		rc = Stream.of(FAIL_PATTERNS_DUP)
				.filter(p -> p.matcher(b.getMsg()).matches())
				.map(p -> p.toString())
				.findFirst();
		if (rc.isPresent()) {
			//同一原因で複数出力されるログは識別のため"（）"を付加する
			return DUP_FAILED_MSG;
		}
		/*
		Pattern ptn = Pattern.compile("VPN Tunneling: Session started for user with IPv4 address [\\d\\.]+, hostname [\\w\\.-]+");
		if (ptn.matcher(b.getMsg()).matches()) {
			return ptn.toString();
		}
		*/
		if (!b.getMsg().contains("failed")) {
			// failed が含まれないメッセージは集約する
			return INFO_SUMMARY_MSG;
		}
		log.warning("(Pattern): \"" + b.getMsg() + "\"");
		return "<Warn>" + b.getMsg();
	}

	@Override
	public List<AccessLogSummary> call(Stream<String> stream)
			throws Exception {
		final List<AccessLogSummary> list = Collections.synchronizedList(new LinkedList<>());;
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
						AccessLogSummary msg;

						if (INFO_SUMMARY_MSG.equals(pattern)) {
						}
						else if (DUP_FAILED_MSG.equals(pattern)) {
						}
						else {
							msg = new AccessLogSummary(b, pattern, isp);
							list.add(msg);
						}
					}
					else {
						addrErrs.add(b.getAddr());
					}
				});
		return list;
	}

	@Override
	public void report(final PrintWriter out, final List<AccessLogSummary> list)
	{
		out.println("発生日時\t国\tISP/プロジェクト\tアドレス\tユーザID\tメッセージ\tロール");
		list.forEach((msg) -> {
			out.println(Stream.of(msg.getFirstDate()
					, msg.getIsp().getCountry()
					, msg.getIsp().getName()
					, msg.getAddr().toString()
					, msg.getId()
					, msg.getPattern()
					, String.join(",", msg.getRoles())
					)
					.collect(Collectors.joining("\t"))
					);
		});
	}

	public static void main(String... argv) {
		if (argv.length < 2) {
			System.err.println("usage: java logcheck.Checker23 knownlist maglist [accesslog...]");
			System.exit(1);
		}

		int rc = 0;
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			//Checker23 application = container.instance().select(Checker23.class, new AnnotationLiteral<UseChecker23>(){
			Checker23 application = container.select(Checker23.class, new AnnotationLiteral<UseChecker23>(){
				private static final long serialVersionUID = 1L;
			}).get();
			application.init(argv[0], argv[1]).start(argv, 2);
		}
		catch (Exception ex) {
			// WeldでStrackTraceが出力されるので、ここで出力の必要はない
			//ex.printStackTrace(System.err);
			rc = 1;
		}
		System.exit(rc);
	}
}
