package logcheck;

import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.log.AccessLogBean;
import logcheck.log.AccessLogSummary;
import logcheck.mag.MagList;
import logcheck.util.net.NetAddr;

/*
 * ログ解析用ツール2：
 * VPNログをExcelで読み込めるTSV形式に変換する
 * なお、このツールは、ユーザ、メッセージ単位の集約を行わないため、1ログメッセージに対して1レコードを出力する。
 * このため、1か月ログを処理した場合、後続に32bit版のExcelを使用する場合、ファイル読込でエラーが発生する可能性がある。
 */
public class Checker9 extends AbstractChecker<List<AccessLogSummary>> {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	@Inject private Logger log;

	private String select;
//	private final List<AccessLogSummary> list = new Vector<>(1000000);

	private static final Pattern[] ALL_PATTERNS;
	static {
		ALL_PATTERNS = new Pattern[INFO_PATTERNS.length + FAIL_PATTERNS.length + FAIL_PATTERNS_DUP.length];
		System.arraycopy(INFO_PATTERNS, 0, ALL_PATTERNS, 0, INFO_PATTERNS.length);
		System.arraycopy(FAIL_PATTERNS, 0, ALL_PATTERNS, INFO_PATTERNS.length, FAIL_PATTERNS.length);
		System.arraycopy(FAIL_PATTERNS_DUP, 0, ALL_PATTERNS, INFO_PATTERNS.length + FAIL_PATTERNS.length, FAIL_PATTERNS_DUP.length);
	}

	public Checker9 init(String select, String knownfile, String magfile) throws Exception {
		this.select = select;
		this.knownlist.load(knownfile);
		this.maglist.load(magfile);
		return this;
	}

	// ログのメッセージ部分はPatternの正規化表現で集約するため、対象ログが一致したPattern文字列を取得する
	protected String getPattern(AccessLogBean b) {
		Optional<String> rc = Stream.of(ALL_PATTERNS)
				.filter(p -> p.matcher(b.getMsg()).matches())
				.map(p -> p.toString())
				.findFirst();
		if (rc.isPresent()) {
			return rc.get();
		}
		log.warning("(Pattern): \"" + b.getMsg() + "\"");
		return b.getMsg();
	}

	public List<AccessLogSummary> call(Stream<String> stream) throws Exception {
		final List<AccessLogSummary> list = new Vector<>(1000000);
		stream//.parallel()
				.filter(AccessLog::test)
				.filter(s -> select.startsWith("-") || s.startsWith(select))
				.map(AccessLog::parse)
				.forEach(b -> {
					String pattern = getPattern(b);

					NetAddr addr = b.getAddr();
					IspList isp = maglist.get(addr);
					if (isp == null) {
						isp = knownlist.get(addr);
					}

					if (isp != null) {
						AccessLogSummary msg = new AccessLogSummary(b, pattern, isp);
						list.add(msg);
					}
					else {
						addrErrs.add(b.getAddr());
					}
				});
		return list;
	}

	public void report(final PrintWriter out, final List<AccessLogSummary> list) {
		out.println("出力日時\t国\tISP/プロジェクト\tアドレス\tユーザID\tロール\tメッセージ");
		list.forEach(msg -> {
			out.println(Stream.of(msg.getFirstDate()
					, msg.getIsp().getCountry()
					, msg.getIsp().getName()
					, msg.getAddr().toString()
					, msg.getId()
					, String.join(",", msg.getRoles())
					, msg.getPattern()
					)
					.collect(Collectors.joining("\t"))
					);
		});
	}

	public static void main(String... argv) {
		Pattern p = Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\d");
		if (argv.length < 3) {
			System.err.println("usage: java logcheck.Checker9 yyyy-mm-dd knownlist maglist [accesslog...]");
			System.exit(1);
		}
		if (!p.matcher(argv[0]).matches()) {
			System.err.println("usage: java logcheck.Checker9 yyyy-mm-dd knownlist maglist [accesslog...]");
			System.exit(1);
		}

		int rc = 0;
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker9 application = container.select(Checker9.class).get();
			application.init(argv[0], argv[1], argv[2]).start(argv, 3);
		}
		catch (Exception ex) {
//			ex.printStackTrace(System.err);
			rc = 1;
		}
		System.exit(rc);
	}
}
