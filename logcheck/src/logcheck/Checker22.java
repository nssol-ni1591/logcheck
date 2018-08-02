package logcheck;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import logcheck.log.AccessLogBean;
import logcheck.util.weld.WeldWrapper;

/*
 * ログ解析用の集約ツール1'：
 * 国 > ISP > クライアントIP > メッセージ  > ID 毎にログ数を集計する。
 * 利用方法としては、プログラムの出力を直接参照するのではなく、Excelに読み込ませpivotで解析する想定のためTSV形式で出力する。
 * なお、このツールでは、正常系ログの集約処理は行わない。
 */
public class Checker22 extends Checker8 {

	// ログのメッセージ部分はPatternの正規化表現で集約するため、対象ログが一致したPattern文字列を取得する
	@Override
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

	public static void main(String... argv) {
		int rc = new WeldWrapper(Checker22.class).weld(2, argv);
		System.exit(rc);
	}
}
