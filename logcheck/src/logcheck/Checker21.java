package logcheck;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import logcheck.fw.FwLog;
import logcheck.fw.FwLogSummary;
import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.mag.MagList;
import logcheck.sdc.SdcList;
import logcheck.util.net.NetAddr;

/*
 * FWログの集約処理：
 * FWログを読込、コレクションにログ情報を登録する。出力結果は、TSV形式とし、Excelでの利用を想定している。
 * なお、コレクションへの登録に際し、送信元アドレス、送信先アドレス、送信先ポートが等しいエントリが存在する場合は、
 * 該当エントリのログ回数の加算、初回日時（ログは新しいログ順で登録されているため）を更新する。
 */
public class Checker21 extends AbstractChecker<Set<FwLogSummary>> {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;
	@Inject private SdcList sdclist;

	public Checker21 init(String knownfile, String magfile, String sdcfile) throws Exception {
		this.knownlist.load(knownfile);
		this.maglist.load(magfile);
		this.sdclist.load(sdcfile);
		return this;
	}

	private IspList getIspList(NetAddr addr) {
		IspList isp = sdclist.get(addr);
		if (isp != null) {
			return isp;
		}
		isp = maglist.get(addr);
		if (isp != null) {
			return isp;
		}
		isp = knownlist.get(addr);
		if (isp != null) {
			return isp;
		}

		isp = new IspList(addr.toString(), "unknown");
		return isp;
	}
	public Set<FwLogSummary> call(Stream<String> stream) throws Exception {
		Set<FwLogSummary> list = new TreeSet<>();
		stream//.parallel()			parallelでは java.util.ConcurrentModificationException が発生
				.filter(FwLog::test)
				.map(FwLog::parse)
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
						sum.update(b);
					}
				});
		return list;
	}

	public void report(Set<FwLogSummary> map) {
		System.out.println("出現日時\t最終日時\t接続元国\t接続元名\t接続元IP\t接続先国\t接続先名\t接続先IP\t接続先ポート\tログ数");

		map.forEach(s -> {
			System.out.println(
					new StringBuilder(s.getFirstDate() == null ? "" : s.getFirstDate())
					.append("\t")
					.append(s.getLastDate())
					.append("\t")
					.append(s.getSrcIsp().getCountry())
					.append("\t")
					.append(s.getSrcIsp().getName())
					.append("\t")
					.append(s.getSrcAddr())
					.append("\t")
					.append(s.getDstIsp().getCountry())
					.append("\t")
					.append(s.getDstIsp().getName())
					.append("\t")
					.append(s.getDstAddr())
					.append("\t")
					.append(s.getDstPort())
					.append("\t")
					.append(s.getCount())
					);
		});
	}

	public static void main(String... argv) {
		if (argv.length < 3) {
			System.err.println("usage: java logcheck.Checker21 knownlist maglist sdclist [accesslog...]");
			System.exit(1);
		}

		int rc = 0;
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker21 application = container.instance().select(Checker21.class).get();
			application.init(argv[0], argv[1], argv[2]).start(argv, 3);
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
			rc = 1;
		}
		System.exit(rc);
	}

}
