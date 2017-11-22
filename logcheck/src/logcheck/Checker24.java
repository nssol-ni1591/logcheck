package logcheck;

import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import logcheck.log.AccessLogSummary;

/*
 * ログ解析用の集約ツール1：
 * 国 > ISP > クライアントIP > メッセージ  > ID 毎にログ数を集計する。
 * 利用方法としては、プログラムの出力を直接参照するのではなく、Excelに読み込ませpivotで解析する想定のためTSV形式で出力する。
 * なお、このツールでは、正常系ログは集約を行う。
 */
public class Checker24 extends Checker23 {

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
		if (argv.length < 2) {
			System.err.println("usage: java logcheck.Checker24 knownlist maglist [accesslog...]");
			System.exit(1);
		}

		int rc = 0;
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker24 application = container.select(Checker24.class).get();
			application.init(argv[0], argv[1]).start(argv, 2);
		}
		catch (Exception ex) {
			rc = 1;
		}
		System.exit(rc);
	}
}
