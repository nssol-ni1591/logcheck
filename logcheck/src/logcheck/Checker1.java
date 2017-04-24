package logcheck;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.log.AccessLogSummary;
import logcheck.mag.MagList;
import logcheck.util.NetAddr;

/*
 * アクセスログのソースIPに一致するISP名/企業名を取得し、国別にISP名/企業名と出力ログ数を出力する
 * 第1引数：ISP別 IPアドレスリスト
 * 第2引数：インターネット経由接続先一覧
 * 第3引数以降：アクセスログ
 */
public class Checker1 extends AbstractChecker<Map<String, Map<String, AccessLogSummary>>> {

	private final KnownList knownlist;
	private final MagList maglist;

	public Checker1(String knownfile, String magfile) throws Exception {
		this.knownlist = loadKnownList(knownfile);
		this.maglist = loadMagList(magfile);
	}

	public Map<String, Map<String, AccessLogSummary>> call(Stream<String> stream) throws Exception {
		Map<String, Map<String, AccessLogSummary>> map = new TreeMap<>();
		stream.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.forEach(b -> {
					NetAddr addr = b.getAddr();
					// magリストに登録されていないログを、国別、ISP別で集計する
					IspList isp = maglist.get(addr);
					if (isp == null) {
						isp = knownlist.get(addr);
						if (isp != null) {
							Map<String, AccessLogSummary> summaryMap = map.get(isp.getCountry());
							if (summaryMap == null) {
								summaryMap = new HashMap<>();
								map.put(isp.getCountry(), summaryMap);
							}

							AccessLogSummary summary = summaryMap.get(isp.getName());
							if (summary == null) {
								summary = new AccessLogSummary(isp.getName(), isp.getCountry());
								summaryMap.put(isp.getName(), summary);
							}
							summary.addAddress(addr);	//summaryのコレクションはSetなので重複はなし
						}
						else {
							// knownリストに登録されていないログはエラー出力
							System.err.println("unknown ip: addr=" + addr + ", date=" + b.getDate());
						}
					}
				});
		return map;
	}

	public void report(Map<String, Map<String, AccessLogSummary>> map) {
		System.out.println();
		map.keySet().forEach(country -> {
			int sum = map.get(country).values().stream().mapToInt(als -> als.sum()).sum();
			System.out.println(country + " : " + sum);

			map.get(country).values().stream().forEach(log -> {
				System.out.printf("\t%s : sum=%d\n", log.getName(), log.sum());
				log.keySet().forEach(addr -> { System.out.printf("\t\t%s : sum=%d\n", addr, log.get(addr)); });
			});
			System.out.println();
		});
	}

	public static void main(String ... argv) {
		if (argv.length < 3) {
			System.err.println("usage: java logcheck.Checker knownlist maglist accesslog...");
			System.exit(1);
		}

		try {
			new Checker1(argv[0], argv[1]).start(argv, 2);;
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
		System.exit(1);
	}
}
