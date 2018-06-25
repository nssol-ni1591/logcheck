package logcheck;

import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.inject.Inject;

import logcheck.log.AccessLogBean;
import logcheck.util.weld.WeldWrapper;

/*
 * ログ解析用の集約ツール1：
 * 国 > ISP > クライアントIP > メッセージ  > ID 毎にログ数を集計する。
 * 利用方法としては、プログラムの出力を直接参照するのではなく、Excelに読み込ませpivotで解析する想定のためTSV形式で出力する。
 * なお、このツールでは、異常系ログは個々のメッセージ単位で出力。正常系ログは集約を行う。
 */
public class Checker25 extends Checker23 {

	@Inject private transient Logger log;

	@Override
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
			return rc.get();
		}
		if (!b.getMsg().contains("failed")) {
			// failed が含まれないメッセージは集約する
			return INFO_SUMMARY_MSG;
		}
		log.warning("(Pattern): \"" + b.getMsg() + "\"");
		return "<Warn>" + b.getMsg();
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper<Checker25>(Checker25.class).weld(2, argv);
		System.exit(rc);
	}
}
