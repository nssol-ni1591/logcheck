package logcheck;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;

import logcheck.isp.IspList;
import logcheck.isp.IspMap;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.mag.MagList;
import logcheck.util.NetAddr;

/*
 * ISP > IPアドレス > メッセージ毎にログ数を集計する
 */
public class Checker3 extends AbstractChecker<Map<String, IspMap<Map<String, Integer>>>> {

	private final KnownList knownlist;
	private final MagList maglist;
	/*
	private static final Pattern[] patterns = {
			Pattern.compile("Primary authentication failed for [\\S ]+ from \\S+"),
			Pattern.compile("Testing Source IP realm restrictions failed for \\S+\\s*"),
			Pattern.compile("Testing Password realm restrictions failed for [\\S ]+ , with certificate .+"),
			Pattern.compile("Testing Certificate realm restrictions failed for .+"),
			Pattern.compile("Host Checker policy 'MAC_Address_Filter' failed on host .+"),
			Pattern.compile("The X\\.509 certificate for .+; Detail: 'certificate revoked' "),
	};
	*/

	public Checker3(String knownfile, String magfile) throws Exception {
		this.knownlist = loadKnownList(knownfile);
		this.maglist = loadMagList(magfile);
	}

	public Map<String, IspMap<Map<String, Integer>>> call(Stream<String> stream) throws Exception {
		Map<String, IspMap<Map<String, Integer>>> map = new TreeMap<>();
		stream.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.forEach(b -> {
					// メッセージにIPアドレスなどが含まれるログは、それ以外の部分を比較対象とするための前処理
					Optional<String> rc = Stream.of(FAIL_PATTERNS)
							.filter(p -> p.matcher(b.getMsg()).matches())
							.map(p -> p.toString())
							.findFirst();
					String m = rc.isPresent() ? rc.get() : b.getMsg();

					NetAddr addr = b.getAddr();
					IspList isp = maglist.get(addr);
					if (isp == null) {
						isp = knownlist.get(addr);
					}

					if (isp != null) {
						IspMap<Map<String, Integer>> ispmap = map.get(isp.getName());
						if (ispmap == null) {
							ispmap = new IspMap<>(isp.getName(), isp.getCountry());
							map.put(isp.getName(), ispmap);
						}

						Map<String, Integer> client = ispmap.get(addr);
						if (client == null) {
							client = new TreeMap<>();
							ispmap.put(addr, client);
						}

						Integer count = client.get(m);
						if (count == null) {
							count = new Integer(0);
						}
						count += 1;
						client.put(m, count);
					}
					else {
						System.err.println("unknown ip: addr=" + addr);
					}
				});
		return map;
	}

	public void report(Map<String, IspMap<Map<String, Integer>>> map) {
		map.values().forEach(isp -> {
			System.out.println();
			System.out.println(isp.getName() + (isp.getCountry() == null ? "" : " (" + isp.getCountry() + ")") + " : ");
			isp.keySet().forEach(addr -> {
				Map<String, Integer> msgs = isp.get(addr);
				System.out.println("\t" + addr + " : ");
				msgs.keySet().forEach(msg -> {
					System.out.println("\t\t[ " + msg + " ] : " + msgs.get(msg));
				});
			});
		});
		System.out.println();
	}

	public static void main(String ... argv) {
		if (argv.length < 2) {
			System.err.println("usage: java logcheck.Checker3 knownlist maglist [accesslog...]");
			System.exit(1);
		}

		try {
			new Checker3(argv[0], argv[1]).start(argv, 2);
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
		System.exit(1);
	}
}
