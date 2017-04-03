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
 * 国 > ISP > クライアントIP > メッセージ  > ID 毎にログ数を集計する
 */
public class Checker8 extends AbstractChecker<Map<String, Map<IspList, Map<NetAddr, Map<String, Map<String, MsgBean>>>>>> {

	private final KnownList knownlist;
	private final MagList maglist;
	private static final String INFO_SUMMARY_MSG = "<><><> Information message summary <><><>";

	public Checker8(String knownfile, String magfile) throws IOException {
		this.knownlist = loadKnownList(knownfile);
		this.maglist = loadMagList(magfile);
	}

	public Map<String, Map<IspList, Map<NetAddr, Map<String, Map<String, MsgBean>>>>> call(Stream<String> stream)
			throws IOException {
		Map<String, Map<IspList, Map<NetAddr, Map<String, Map<String, MsgBean>>>>> map = new TreeMap<>();
		stream.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.forEach(b -> {
					String pattern;
					// メッセージにIPアドレスなどが含まれるログは、それ以外の部分を比較対象とするための前処理
					Optional<String> rc = Stream.of(FAIL_PATTERNS)
							.filter(p -> p.matcher(b.getMsg()).matches())
							.map(p -> p.toString())
							.findFirst();
					if (rc.isPresent()) {
						pattern = rc.get();
					}
					else {
						rc = Stream.of(FAIL_PATTERNS_ALL)
								.filter(p -> p.matcher(b.getMsg()).matches())
								.map(p -> p.toString())
								.findFirst();
						if (rc.isPresent()) {
							pattern = "(" + rc.get() + ")";
						}
						else if (!b.getMsg().contains("failed")) {
							pattern = INFO_SUMMARY_MSG;
						}
						else {
							System.err.println("ERROR: \"" + b.getMsg() + "\"");
							pattern = b.getMsg();
						}
					}

					NetAddr addr = b.getAddr();
					IspList isp = maglist.get(addr);
					if (isp == null) {
						isp = knownlist.get(addr);
					}

					if (isp != null) {
						Map<IspList, Map<NetAddr, Map<String, Map<String, MsgBean>>>> ispmap;
						Map<NetAddr, Map<String, Map<String, MsgBean>>> addrmap;
						Map<String, Map<String, MsgBean>> idmap;
						Map<String, MsgBean> msgmap;
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
							msg = new MsgBean(b, pattern);
							msgmap.put(pattern, msg);
						}
						else {
							msg.update(b.getDate());
						}

					} else {
							System.err.println("unknown ip: addr=" + addr);
					}
				});
		return map;
	}

	public void report(Map<String, Map<IspList, Map<NetAddr, Map<String, Map<String, MsgBean>>>>> map) {
		System.out.println("国\tISP/プロジェクト\tアドレス\tユーザID\tメッセージ\t出現日時\t最終日時\tログ数\tISP合計");
		map.forEach((country, ispmap) -> {

			ispmap.forEach((isp, addrmap) -> {
				int sumIspLog = addrmap.values().stream().mapToInt(idmap -> {
					return idmap.values().stream().mapToInt(msgmap -> {
						return msgmap.values().stream().mapToInt(msg -> msg.getCount()).sum();
					}).sum();
				}).sum();

				addrmap.forEach((addr, idmap) -> {

					idmap.forEach((id, msgmap) -> {

						msgmap.forEach((pattern, msg) -> {
							System.out.println(
									new StringBuilder("".equals(country) ? "<MAG>" : country)
											.append("\t")
											.append(isp)
											.append("\t")
											.append(addr)
											.append("\t")
											.append(id)
											.append("\t")
											.append(pattern)
											.append("\t")
											.append(msg.getFirstDate())
											.append("\t")
											.append(msg.getLastDate())
											.append("\t")
											.append(msg.getCount())
											.append("\t")
											.append(sumIspLog));
						});
					});
				});
			});
		});
	}

	public static void main(String... argv) {
		if (argv.length < 2) {
			System.err.println("usage: java logcheck.Checker4 knownlist maglist [accesslog...]");
			System.exit(1);
		}

		try {
			new Checker8(argv[0], argv[1]).start(argv, 2);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
		System.exit(1);
	}
}
