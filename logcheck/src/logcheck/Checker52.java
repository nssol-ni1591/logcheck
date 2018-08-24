package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Set;

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
		out.println(String.join("\t"
				, "出現日時"
				, "最終日時"
				, "接続元識別"
				, "接続元NW"
				, "接続元IP"
				, "接続先識別"
				, "接続先NW"
				, "接続先IP"
				, "接続先ポート"
				, "ログ数"
				));
		list.forEach(s -> 
			out.println(String.join("\t"
					, s.getFirstDate() == null ? "" : s.getFirstDate()
					, s.getLastDate()
					, s.getSrcIsp().getCountry()
					, s.getSrcIsp().getName()
					, s.getSrcIsp().getHostname(s.getSrcAddr())	//s.getSrcAddr().toString()
					, s.getDstIsp().getCountry()
					, s.getDstIsp().getName()
					, s.getDstIsp().getHostname(s.getDstAddr())	//s.getDstAddr().toString()
					, String.valueOf(s.getDstPort())
					, String.valueOf(s.getCount())
					))
				);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper(Checker52.class).weld(3, argv);
		System.exit(rc);
	}

}
