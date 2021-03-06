package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import logcheck.annotations.UseChecker50;
import logcheck.fw.FwLog;
import logcheck.fw.FwLogSummary;
import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.known.impl.net.PrivateAddrList;
import logcheck.mag.MagList;
import logcheck.sdc.SdcList;
import logcheck.util.NetAddr;
import logcheck.util.WeldWrapper;

/*
 * FWログの集約処理：
 * FWログを読込、コレクションにログ情報を登録する。出力結果は、TSV形式とし、Excelでの利用を想定している。
 * なお、コレクションへの登録に際し、送信元アドレス、送信先アドレス、送信先ポートが等しいエントリが存在する場合は、
 * 該当エントリのログ回数の加算、初回日時（ログは新しいログ順で登録されているため）を更新する。
 */
@UseChecker50
public class Checker50 extends AbstractChecker<Set<FwLogSummary>> {

	protected KnownList knownlist = new PrivateAddrList();
	@Inject protected MagList maglist;
	@Inject protected SdcList sdclist;

	public void init(String...argv) throws IOException, ClassNotFoundException, SQLException {
		this.knownlist.load(argv[0]);
		this.maglist.load(argv[1]);
		this.sdclist.load(argv[2]);
	}

	protected IspList getIspList(NetAddr addr) {
		IspList isp = sdclist.get(addr);
		if (isp != null) {
			return isp;
		}
		isp = maglist.get(addr);
		if (isp != null) {
			return isp;
		}
		isp = knownlist.get(addr);
		// knownlist.get(...)はnullを返却しない

		//isp = new IspListImpl("社外サイト", "社外")
		return isp;
	}

	@Override
	public Set<FwLogSummary> call(Stream<String> stream) {
		final Set<FwLogSummary> list = new TreeSet<>();
		stream//.parallel()			// parallelでは java.util.ConcurrentModificationException が発生
				.map(FwLog::parse)
				.filter(Objects::nonNull)
				.forEach(b -> {
					Optional<FwLogSummary> op = list.stream()
							.filter(isp -> (isp.getDstPort() - b.getDstPort()) == 0)
							.filter(isp -> isp.getSrcAddr().equals(b.getSrcIp()))
							.filter(isp -> isp.getDstAddr().equals(b.getDstIp()))
							.findFirst();
					FwLogSummary sum = op.isPresent() ? op.get() : null;
					if (sum == null) {
						IspList srcIsp = getIspList(b.getSrcIp());
						IspList dstIsp = getIspList(b.getDstIp());
						sum = new FwLogSummary(b, srcIsp, dstIsp);
						list.add(sum);
					}
					else {
						sum.update(b.getDate());
					}
				});
		return list;
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
					, s.getSrcAddr().toString()
					, s.getDstIsp().getCountry()
					, s.getDstIsp().getName()
					, s.getDstAddr().toString()
					, String.valueOf(s.getDstPort())
					, String.valueOf(s.getCount())
					))
				);
	}

	@Override
	public String usage(String name) {
		return String.format("usage: java %s knownlist maglist sdclist [fwlog...]", name);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper(Checker50.class).weld(new AnnotationLiteral<UseChecker50>(){
			private static final long serialVersionUID = 1L;
		}, 3, argv);
		System.exit(rc);
	}

}
