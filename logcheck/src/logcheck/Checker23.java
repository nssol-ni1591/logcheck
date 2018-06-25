package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import logcheck.annotations.UseChecker23;
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
@UseChecker23
public class Checker23 extends AbstractChecker<List<AccessLogSummary>> {

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
		// 同一原因エラーログは集約する
		rc = Stream.of(FAIL_PATTERNS_DUP)
				.filter(p -> p.matcher(b.getMsg()).matches())
				.map(Pattern::toString)
				.findFirst();
		if (rc.isPresent()) {
			return DUP_FAILED_MSG;
		}
		// failed が含まれないメッセージは集約する
		if (!b.getMsg().contains("failed")) {
			return INFO_SUMMARY_MSG;
		}
		log.warning("(Pattern): \"" + b.getMsg() + "\"");
		return "<Warn>" + b.getMsg();
	}

	@Override
	public List<AccessLogSummary> call(Stream<String> stream) {
		final List<AccessLogSummary> list = Collections.synchronizedList(new LinkedList<>());
		stream//.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.forEach(b -> {
					String pattern = getPattern(b);
					AccessLogSummary msg;

					if (INFO_SUMMARY_MSG.equals(pattern)) {
						// 正常メッセージは出力しない⇒出力データ削減
					}
					else if (DUP_FAILED_MSG.equals(pattern)) {
						// 同一原因のエラーメッセージは出力しない⇒出力データ削減
					}
					else {
						// Ispへの変換は出力対象のメッセージの場合だけ実行すればよい
						NetAddr addr = b.getAddr();
						IspList isp = maglist.get(addr);
						if (isp == null) {
							isp = knownlist.get(addr);
							if (isp == null) {
								addrErrs.add(b.getAddr());
								return;
							}
						}

						msg = new AccessLogSummary(b, pattern, isp);
						list.add(msg);
					}
				});
		return list;
	}

	@Override
	public void report(final PrintWriter out, final List<AccessLogSummary> list) {
		out.println("発生日時\t発生日\t国\tISP/プロジェクト\tアドレス\tユーザID\tメッセージ\tロール");
		list.forEach(msg -> 
			out.println(Stream.of(msg.getFirstDate()
					, msg.getFirstDate().substring(0, 10)
					, msg.getIsp().getCountry()
					, msg.getIsp().getName()
					, msg.getAddr().toString()
					, msg.getId()
					, msg.getPattern()
					, String.join(",", msg.getRoles())
					)
					.collect(Collectors.joining("\t"))
					)
				);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper<Checker23>(Checker23.class).weld(new AnnotationLiteral<UseChecker23>(){
			private static final long serialVersionUID = 1L;
		}, 2, argv);
		System.exit(rc);
	}
}
