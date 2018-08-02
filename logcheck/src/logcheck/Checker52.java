package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import logcheck.annotations.UseSdcListNW;
import logcheck.fw.FwLogSummary;
import logcheck.sdc.SdcList;
import logcheck.util.WeldWrapper;

/*
 * FWログの集約処理：
 * FWログを読込、コレクションにログ情報を登録する。出力結果は、TSV形式とし、Excelでの利用を想定している。
 * なお、コレクションへの登録に際し、送信元アドレス、送信先アドレス、送信先ポートが等しいエントリが存在する場合は、
 * 該当エントリのログ回数の加算、初回日時（ログは新しいログ順で登録されているため）を更新する。
 */
public class Checker52 extends Checker50 {

	@Inject @UseSdcListNW protected SdcList sdclistNW;

	@Override
	public void init(String...argv) throws IOException, ClassNotFoundException, SQLException {
		super.sdclist = this.sdclistNW;
		super.init(argv);
	}

	@Override
	public void report(final PrintWriter out, final Set<FwLogSummary> list) {
		out.println("出現日時\t最終日時\t接続元識別\t接続元NW\t接続元IP\t接続先識別\t接続先NW\t接続先IP\t接続先ポート\tログ数");
		list.forEach(s -> 
			out.println(Stream.of(s.getFirstDate() == null ? "" : s.getFirstDate()
					, s.getLastDate()
					, s.getSrcIsp().getCountry()
					, s.getSrcIsp().getName()
					, s.getSrcIsp().getHostname(s.getSrcAddr())	//s.getSrcAddr().toString()
					, s.getDstIsp().getCountry()
					, s.getDstIsp().getName()
					, s.getDstIsp().getHostname(s.getDstAddr())	//s.getDstAddr().toString()
					, String.valueOf(s.getDstPort())
					, String.valueOf(s.getCount())
					)
					.collect(Collectors.joining("\t")))
				);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper(Checker52.class).weld(3, argv);
		System.exit(rc);
	}

}
