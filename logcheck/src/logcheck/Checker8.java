package logcheck;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import logcheck.annotations.UseChecker8;
import logcheck.isp.Isp;
import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.log.AccessLogBean;
import logcheck.log.AccessLogSummary;
import logcheck.mag.MagList;
import logcheck.util.net.NetAddr;

/*
 * ログ解析用の集約ツール1：
 * 国 > ISP > クライアントIP > メッセージ  > ID 毎にログ数を集計する。
 * 利用方法としては、プログラムの出力を直接参照するのではなく、Excelに読み込ませpivotで解析する想定のためTSV形式で出力する。
 * なお、このツールでは、正常系ログは集約を行う。
 */
@UseChecker8
public class Checker8 extends AbstractChecker<Map<String, Map<Isp, Map<NetAddr, Map<String, Map<String, AccessLogSummary>>>>>> {

	@Inject protected KnownList knownlist;
	@Inject protected MagList maglist;

	@Inject private Logger log;

	private Map<String, Map<Isp, Map<NetAddr, Map<String, Map<String, AccessLogSummary>>>>> map = new TreeMap<>();

	public Checker8 init(String knownfile, String magfile) throws Exception {
		this.knownlist.load(knownfile);
		this.maglist.load(magfile);
		return this;
	}

	// ログのメッセージ部分はPatternの正規化表現で集約するため、対象ログと一致したPattern文字列を取得する
	protected String getPattern(AccessLogBean b) {
		Optional<String> rc = Stream.of(FAIL_PATTERNS)
				.filter(p -> p.matcher(b.getMsg()).matches())
				.map(p -> p.toString())
				.findFirst();
		if (rc.isPresent()) {
			return rc.get();
		}

		rc = Stream.of(FAIL_PATTERNS_DUP)
				.filter(p -> p.matcher(b.getMsg()).matches())
				.map(p -> p.toString())
				.findFirst();
		if (rc.isPresent()) {
			//同一原因で複数出力されるログは識別のため"（）"を付加する
			return "(" + rc.get() + ")";
		}
		if (!b.getMsg().contains("failed")) {
			// failed が含まれないメッセージは集約する
			return INFO_SUMMARY_MSG;
		}
//		System.err.println("ERROR: \"" + b.getMsg() + "\"");
		log.warning("(Pattern): \"" + b.getMsg() + "\"");
		return b.getMsg();
	}

	@Override
	public Map<String, Map<Isp, Map<NetAddr, Map<String, Map<String, AccessLogSummary>>>>> call(Stream<String> stream) throws Exception {
		stream//.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.forEach(b -> {
//					log.info(b.toString());

					String pattern = getPattern(b);

					NetAddr addr = b.getAddr();
					IspList isp = maglist.get(addr);
					if (isp == null) {
						isp = knownlist.get(addr);
					}

					if (isp != null) {
						Map<Isp, Map<NetAddr, Map<String, Map<String, AccessLogSummary>>>> ispmap;
						Map<NetAddr, Map<String, Map<String, AccessLogSummary>>> addrmap;
						Map<String, Map<String, AccessLogSummary>> idmap;
						Map<String, AccessLogSummary> msgmap;
						AccessLogSummary msg;

						ispmap = map.get(isp.getCountry());
						if (ispmap == null) {
							ispmap = new TreeMap<>();
							map.put(isp.getCountry(), ispmap);
						}

						addrmap = ispmap.get(isp);
						if (addrmap == null) {
							addrmap = new TreeMap<>();
							ispmap.put(isp, addrmap);
						}

						idmap = addrmap.get(addr);
						if (idmap == null) {
							idmap = new TreeMap<>();
							addrmap.put(addr, idmap);
						}

						msgmap = idmap.get(b.getId());
						if (msgmap == null) {
							msgmap = new TreeMap<>();
							idmap.put(b.getId(), msgmap);
						}

						msg = msgmap.get(pattern);
						if (msg == null) {
							msg = new AccessLogSummary(b, pattern);
							msgmap.put(pattern, msg);
						}
						else {
							msg.update(b);
						}

					}
					else {
//						System.err.println("unknown ip: addr=" + addr);
//						log.warning("unknown ip: addr=" + addr);
						addrErrs.add(b.getAddr());
					}
				});
		return map;
	}

	@Override
	public void report() {
		System.out.println("国\tISP/プロジェクト\tアドレス\tユーザID\tメッセージ\tロール\t初回日時\t最終日時\tログ数");
		map.forEach((country, ispmap) -> {

			ispmap.forEach((isp, addrmap) -> {

				addrmap.forEach((addr, idmap) -> {

					idmap.forEach((id, msgmap) -> {

						msgmap.forEach((pattern, msg) -> {
							System.out.println(
									new StringBuilder(country)
											.append("\t").append(isp)
											.append("\t").append(addr)
											.append("\t").append(id)
											.append("\t").append(pattern)
											.append("\t").append(msg.getRoles())
											.append("\t").append(msg.getFirstDate())
											.append("\t").append(msg.getLastDate())
											.append("\t").append(msg.getCount())
//											.append("\t").append(sumIspLog)
											);
						});
					});
				});
			});
		});
	}

	public static void main(String... argv) {
		if (argv.length < 2) {
			System.err.println("usage: java logcheck.Checker8 knownlist maglist [accesslog...]");
			System.exit(1);
		}

		int rc = 0;
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			//Checker8 application = container.instance().select(Checker8.class).get();
			/*
			@SuppressWarnings("serial")
			Checker8 application = (Checker8) container.instance().select(new AnnotationLiteral<UseChecker8>(){
				private static final long serialVersionUID = 1L;
			}).get();
			*/
			Checker8 application = container.instance().select(Checker8.class, new AnnotationLiteral<UseChecker8>(){
				private static final long serialVersionUID = 1L;
			}).get();
			application.init(argv[0], argv[1]).start(argv, 2);
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
			rc = 1;
		}
		System.exit(rc);
	}
}
