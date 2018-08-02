package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.log.AccessLogBean;
import logcheck.log.AccessLogSummary;
import logcheck.mag.MagList;
import logcheck.util.net.NetAddr;
import logcheck.util.weld.WeldWrapper;

/*
 * ログ解析用ツール2：
 * VPNログをExcelで読み込めるTSV形式に変換する
 * なお、このツールは、ユーザ、メッセージ単位の集約を行わないため、1ログメッセージに対して1レコードを出力する。
 * このため、1か月ログを処理した場合、後続に32bit版のExcelを使用する場合、ファイル読込でエラーが発生する可能性がある。
 */
public class Checker9 extends AbstractChecker<List<AccessLogSummary>> {

	@Inject private Logger log;

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	private String select;

	public void init(String...argv) throws IOException, ClassNotFoundException, SQLException {
		this.select = argv[0];
		this.knownlist.load(argv[1]);
		this.maglist.load(argv[2]);
	}

	// ログのメッセージ部分はPatternの正規化表現で集約するため、対象ログが一致したPattern文字列を取得する
	protected String getPattern(AccessLogBean b) {
		Optional<String> rc = Stream.of(ALL_PATTERNS)
				.filter(p -> p.matcher(b.getMsg()).matches())
				.map(Pattern::toString)
				.findFirst();
		if (rc.isPresent()) {
			return rc.get();
		}
		ptnErrs.add(b.getMsg());
		return b.getMsg();
	}

	public List<AccessLogSummary> call(Stream<String> stream) {
		final List<AccessLogSummary> list = new LinkedList<>();
		stream//.parallel()
				.filter(AccessLog::test)
				.filter(s -> select.startsWith("-") || s.startsWith(select))
				.map(AccessLog::parse)
				.forEach(b -> {
					String pattern = getPattern(b);

					NetAddr addr = b.getAddr();
					IspList isp = getIsp(addr, maglist, knownlist);
					if (isp != null) {
						AccessLogSummary msg = new AccessLogSummary(b, pattern, isp);
						list.add(msg);
					}
				});
		return list;
	}

	public void report(final PrintWriter out, final List<AccessLogSummary> list) {
		out.println("出力日時\t国\tISP/プロジェクト\tアドレス\tユーザID\tロール\tメッセージ");
		list.forEach(msg -> 
			out.println(Stream.of(msg.getFirstDate()
					, msg.getIsp().getCountry()
					, msg.getIsp().getName()
					, msg.getAddr().toString()
					, msg.getId()
					, String.join(",", msg.getRoles())
					, msg.getPattern()
					)
					.collect(Collectors.joining("\t")))
		);
	}

	@Override
	public String usage(String name) {
		return String.format("usage: java %s yyyy-mm-dd knownlist maglist [accesslog...]", name);
	}
	@Override
	public boolean check(int argc, String...argv) {
		Pattern p = Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\d");
		if (argv.length > 1 && !p.matcher(argv[0]).matches()) {
			//Invoke method(s) only conditionally.
			String msg = usage("Checker9");
			log.log(Level.SEVERE, msg);
			return false;
		}
		return true;
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper(Checker9.class).weld(3, argv);
		System.exit(rc);
	}

}
