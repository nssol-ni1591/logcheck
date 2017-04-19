package logcheck;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import logcheck.isp.Isp;
import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.log.AccessLogBean;
import logcheck.mag.MagList;
import logcheck.msg.MsgBean;
import logcheck.util.NetAddr;

/*
 * IP_RANGE_PATTERN に合致するログを検索し、 国 > ISP > クライアントIP > MsgBean 毎にログ数を集計する
 */
public class Checker12 extends AbstractChecker<Map<String, Map<Isp, Map<NetAddr, MsgBean>>>> {

	protected final KnownList knownlist;
	protected final MagList maglist;

	private static final Pattern IP_RANGE_PATTERN = Pattern.compile("Testing Source IP realm restrictions failed for /NSSDC-Auth1 *");

	public Checker12(String knownfile, String magfile) throws Exception {
		this.knownlist = loadKnownList(knownfile);
		this.maglist = loadMagList(magfile);
	}

	public static boolean test(AccessLogBean b) {
		// メッセージにIPアドレスなどが含まれるログは、それ以外の部分を比較対象とするための前処理
		return IP_RANGE_PATTERN.matcher(b.getMsg()).matches();
	}

	public Map<String, Map<Isp, Map<NetAddr, MsgBean>>> call(Stream<String> stream) throws Exception {
		Map<String, Map<Isp, Map<NetAddr, MsgBean>>> map = new TreeMap<>();
		stream.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.filter(Checker12::test)
				.forEach(b -> {
					NetAddr addr = b.getAddr();
					IspList isp = maglist.get(addr);
					if (isp == null) {
						isp = knownlist.get(addr);
					}

					if (isp != null) {
						Map<Isp, Map<NetAddr, MsgBean>> ispmap;
						Map<NetAddr, MsgBean> addrmap;
						MsgBean msg;

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

						msg = addrmap.get(addr);
						if (msg == null) {
							msg = new MsgBean(b, IP_RANGE_PATTERN.toString());
							addrmap.put(addr, msg);
						}
						else {
							msg.update(b);
						}

					} else {
							System.err.println("unknown ip: addr=" + addr);
					}
				});
		return map;
	}

	public void report(Map<String, Map<Isp, Map<NetAddr, MsgBean>>> map) {
		System.out.println("国\tISP/プロジェクト\tアドレス\t初回日時\t最終日時\tログ数");
		map.forEach((country, ispmap) -> {

			ispmap.forEach((isp, addrmap) -> {
				addrmap.forEach((addr, msg) -> {
					System.out.println(
							new StringBuilder(country)
									.append("\t")
									.append(isp)
									.append("\t")
									.append(addr)
									.append("\t")
									.append(msg.getFirstDate())
									.append("\t")
									.append(msg.getLastDate())
									.append("\t")
									.append(msg.getCount()));
				});
			});
		});
	}

	public static void main(String... argv) {
		if (argv.length < 2) {
			System.err.println("usage: java logcheck.Checker12 knownlist maglist [accesslog...]");
			System.exit(1);
		}

		try {
			new Checker12(argv[0], argv[1]).start(argv, 2);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
		System.exit(1);
	}
}
