package logcheck;

import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import logcheck.log.AccessLogBean;

/*
 * ログ解析用の集約ツール1'：
 * 国 > ISP > クライアントIP > メッセージ  > ID 毎にログ数を集計する。
 * 利用方法としては、プログラムの出力を直接参照するのではなく、Excelに読み込ませpivotで解析する想定のためTSV形式で出力する。
 * なお、このツールでは、正常系ログの集約処理は行わない。
 */
public class Checker8a extends Checker8 {

	@Inject private Logger log;

	private static final Pattern[] ALL_PATTERNS;
	static {
		ALL_PATTERNS = new Pattern[INFO_PATTERNS.length + FAIL_PATTERNS.length + FAIL_PATTERNS_DUP.length];
		System.arraycopy(INFO_PATTERNS, 0, ALL_PATTERNS, 0, INFO_PATTERNS.length);
		System.arraycopy(FAIL_PATTERNS, 0, ALL_PATTERNS, INFO_PATTERNS.length, FAIL_PATTERNS.length);
		System.arraycopy(FAIL_PATTERNS_DUP, 0, ALL_PATTERNS, INFO_PATTERNS.length + FAIL_PATTERNS.length, FAIL_PATTERNS_DUP.length);
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
//		System.err.println("ERROR: \"" + b.getMsg() + "\"");
		log.warning("(Pattern): \"" + b.getMsg() + "\"");
		return b.getMsg();
	}

	public static void main(String... argv) {
		if (argv.length < 2) {
			System.err.println("usage: java logcheck.Checker81 knownlist maglist [accesslog...]");
			System.exit(1);
		}

		int rc = 0;
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker8a application = container.instance().select(Checker8a.class).get();
			application.init(argv[0], argv[1]).start(argv, 2);
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
			rc = 1;
		}
		System.exit(rc);
	}
}
