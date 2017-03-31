package logcheck;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;

import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.mag.MagList;
import logcheck.msg.MsgBean;
import logcheck.util.NetAddr;

/*
 * 国 > ISP > メッセージ > クライアントIP 毎にログ数を集計する
 */
public class Checker7 extends AbstractChecker<Map<String, Map<IspList, Map<NetAddr, Map<MsgBean, Integer>>>>> {

	private final KnownList knownlist;
	private final MagList maglist;
	private static final String INFO_SUMMARY_MSG = "<><><> Information message summary <><><>";

	public Checker7(String knownfile, String magfile) throws IOException {
		this.knownlist = loadKnownList(knownfile);
		this.maglist = loadMagList(magfile);
	}

	public Map<String, Map<IspList, Map<NetAddr, Map<MsgBean, Integer>>>> call(Stream<String> stream)
			throws IOException {
		Map<String, Map<IspList, Map<NetAddr, Map<MsgBean, Integer>>>> map = new TreeMap<>();
		stream.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.forEach(b -> {
					// メッセージにIPアドレスなどが含まれるログは、それ以外の部分を比較対象とするための前処理
					Optional<String> rc = Stream.of(FAIL_PATTERNS)
							.filter(p -> p.matcher(b.getMsg()).matches())
							.map(p -> p.toString())
							.findFirst();
					String pattern = rc.isPresent() ? rc.get() : b.getMsg();
					if (!rc.isPresent()) {
						pattern = INFO_SUMMARY_MSG;
					}

					NetAddr addr = b.getAddr();
					IspList isp = maglist.get(addr);
					if (isp == null) {
						isp = knownlist.get(addr);
					}

					if (isp != null) {
						Map<IspList, Map<NetAddr, Map<MsgBean, Integer>>> ispmap;
						Map<NetAddr, Map<MsgBean, Integer>> addrmap;
						Map<MsgBean, Integer> msgmap;
						Integer count;

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

						msgmap = addrmap.get(addr);
						if (msgmap == null) {
							msgmap = new TreeMap<>();
							addrmap.put(addr, msgmap);
						}

						MsgBean msg = new MsgBean(b, pattern);
						count = msgmap.get(msg);
						if (count == null) {
							count = new Integer(0);
						}
						else {
							msg.updateDate(b.getDate());
						}
						count += 1;
						msgmap.put(msg, count);
					} else {
						System.err.println("unknown ip: addr=" + addr);
					}
				});
		return map;
	}

	public void report(Map<String, Map<IspList, Map<NetAddr, Map<MsgBean, Integer>>>> map) {
		System.out.println("国\tISP/プロジェクト\tアドレス\tメッセージ\t出現日時\t最終日時\tログ数\tISP合計");
		map.forEach((country, ispmap) -> {
//			int sum = ispmap.values().stream().mapToInt(msgmap -> {
//				return msgmap.values().stream().mapToInt(addrmap -> {
//					return addrmap.values().stream().mapToInt(c -> c.intValue()).sum();
//				}).sum();
//			}).sum();
//			int sum1 = ispmap.values().stream().mapToInt(msgmap -> {
//				return msgmap.get(INFO_SUMMARY_MSG) == null ? 0 : msgmap.get(INFO_SUMMARY_MSG).values().stream().mapToInt(c -> c.intValue()).sum();
//			}).sum();
//			System.out.println(("".equals(country) ? "<MAGLIST>" : country) +
//				new StringBuilder().append(" : ").append(sum - sum1).append(" / ").append(sum).append(" => ").append((sum - sum1) * 100 / sum).append("%").toString());

			ispmap.forEach((isp, addrmap) -> {
//				int sum2 = msgmap.values().stream().mapToInt(addrmap -> {
//					return addrmap.values().stream().mapToInt(c -> c.intValue()).sum();
//				}).sum();
//				int sum21 = msgmap.get(INFO_SUMMARY_MSG) == null ? 0 : msgmap.get(INFO_SUMMARY_MSG).values().stream().mapToInt(c -> c.intValue()).sum();
//				System.out.println(new StringBuilder().append("\t").append(isp).append(" : ").append(sum2 - sum21).append(" / ").append(sum2).append(" => ").append((sum2 - sum21) * 100 / sum2).append("%"));
				int sumIspLog = addrmap.values().stream().mapToInt(msgmap -> {
					return msgmap.values().stream().mapToInt(c -> c.intValue()).sum();
				}).sum();

				addrmap.forEach((addr, msgmap) -> {
//					int sum3 = addrmap.values().stream().mapToInt(c -> c.intValue()).sum();
//					System.out.println(new StringBuilder().append("\t\t[ ").append(msg).append(" ] : ").append(sum3));

					msgmap.forEach((msg, count) -> {
//						System.out.println(new StringBuilder().append("\t\t\t").append(addr).append(" : ").append(count));
						System.out.println(
								new StringBuilder("".equals(country) ? "<MAGLIST>" : country)
								.append("\t")
								.append(isp)
								.append("\t")
								.append(addr)
								.append("\t")
								.append(msg.getPattern())
								.append("\t")
								.append(msg.getFirstDate())
								.append("\t")
								.append(msg.getLastDate())
								.append("\t")
								.append(count)
								.append("\t")
								.append(sumIspLog));
					});
				});
			});
//			System.out.println();
		});
//		System.out.println();
	}

	public static void main(String... argv) {
		if (argv.length < 2) {
			System.err.println("usage: java logcheck.Checker4 knownlist maglist [accesslog...]");
			System.exit(1);
		}

		try {
			new Checker7(argv[0], argv[1]).start(argv, 2);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
		System.exit(1);
	}
}
