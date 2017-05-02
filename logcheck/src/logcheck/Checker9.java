package logcheck;

import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.regex.Pattern;
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
import logcheck.util.NetAddr;

/*
 * 時間 > ISP > ソースIP > ID > メッセージ 毎にログ数を出力する（集計はしない）
 */
public class Checker9 extends AbstractChecker<List<AccessLogSummary>> {

	private String select;
	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

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

	protected String getPattern(AccessLogBean b) {
		// メッセージにIPアドレスなどが含まれるログは、それ以外の部分を比較対象とするための前処理
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

	public List<AccessLogSummary> call(Stream<String> stream) throws Exception {
		List<AccessLogSummary> list = new Vector<>(1000000);
		stream//.parallel()
				.filter(AccessLog::test)
				.filter(s -> s.startsWith(select))
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
					} else {
//						System.err.println("unknown ip: addr=" + addr);
						log.warning("unknown ip: addr=" + addr);
					}
				});
		return list;
	}

	public void report(List<AccessLogSummary> list) {
		System.out.println("出力日時\t国\tISP/プロジェクト\tアドレス\tユーザID\tロール\tメッセージ");

		list.forEach(msg -> {
			System.out.println(
					new StringBuilder(msg.getFirstDate())
					.append("\t")
					.append(msg.getIsp().getCountry())
					.append("\t")
					.append(msg.getIsp().getName())
					.append("\t")
					.append(msg.getAddr())
					.append("\t")
					.append(msg.getId())
					.append("\t")
					.append(msg.getRoles())
					.append("\t")
					.append(msg.getPattern())
					);
		});
	}

	public static void main(String... argv) {
		if (argv.length < 3) {
			System.err.println("usage: java yyyy-mm-dd logcheck.Checker9 knownlist maglist [accesslog...]");
			System.exit(1);
		}

		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker9 application = container.instance().select(Checker9.class).get();
			application.init(argv[0], argv[1], argv[2]).start(argv, 3);
			System.exit(0);
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
		System.exit(1);
	}
}
