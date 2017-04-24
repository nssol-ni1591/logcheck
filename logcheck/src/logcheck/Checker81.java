package logcheck;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import logcheck.log.AccessLogBean;

/*
 * 国 > ISP > クライアントIP > メッセージ  > ID 毎にログ数を集計する
 */
public class Checker81 extends Checker8 {

	private static final Pattern[] ALL_PATTERNS;
	
	static {
		ALL_PATTERNS = new Pattern[INFO_PATTERNS.length + FAIL_PATTERNS.length + FAIL_PATTERNS_DUP.length];
		System.arraycopy(INFO_PATTERNS, 0, ALL_PATTERNS, 0, INFO_PATTERNS.length);
		System.arraycopy(FAIL_PATTERNS, 0, ALL_PATTERNS, INFO_PATTERNS.length, FAIL_PATTERNS.length);
		System.arraycopy(FAIL_PATTERNS_DUP, 0, ALL_PATTERNS, INFO_PATTERNS.length + FAIL_PATTERNS.length, FAIL_PATTERNS_DUP.length);
	}

	public Checker81(String knownfile, String magfile) throws Exception {
		super(knownfile, magfile);
	}

	protected String getPattern(AccessLogBean b) {
		// メッセージにIPアドレスなどが含まれるログは、それ以外の部分を比較対象とするための前処理
		Optional<String> rc = Stream.of(ALL_PATTERNS)
				.filter(p -> p.matcher(b.getMsg()).matches())
				.map(p -> p.toString())
				.findFirst();
		if (rc.isPresent()) {
			return rc.get();
		}
		System.err.println("ERROR: \"" + b.getMsg() + "\"");
		return b.getMsg();
	}

	public static void main(String... argv) {
		if (argv.length < 2) {
			System.err.println("usage: java logcheck.Checker81 knownlist maglist [accesslog...]");
			System.exit(1);
		}

		try {
			new Checker81(argv[0], argv[1]).start(argv, 2);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
		System.exit(1);
	}
}
