package logcheck.known.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.inject.Alternative;

import logcheck.known.KnownList;
import logcheck.known.KnownListIsp;
import logcheck.known.tsv.TsvKnownList;
import logcheck.util.net.ClientAddr;
import logcheck.util.net.NetAddr;

@Alternative
public class Whois extends LinkedHashSet<KnownListIsp> implements KnownList {

	private static final long serialVersionUID = 1L;

	private static final Pattern[] PTN_NETADDRS = {
			Pattern.compile("% Information related to '(\\d+\\.\\d+\\.\\d+\\.\\d+ - \\d+\\.\\d+\\.\\d+\\.\\d+)'"),
			Pattern.compile("inetnum: +([\\d+\\.\\d+/\\d+]+)"),
			Pattern.compile("\\w?\\.? ?\\[Network Number\\] +(\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+)"),
			Pattern.compile("CIDR: +(\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+)"),
			Pattern.compile("network:IP-Network:(\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+)"),
			Pattern.compile("CIDR: +([\\d+\\./, ]+)"),
			Pattern.compile("IPv4 Address +: (\\d+\\.\\d+\\.\\d+\\.\\d+ - \\d+\\.\\d+\\.\\d+\\.\\d+) \\([/\\d]*\\)"),
			Pattern.compile(" *Netblock: +([\\d+\\./]+)"),
			Pattern.compile("[Aa]uth-?[Aa]rea[=:]([\\d+\\./]+)"),
	};
	private static final Pattern[] PTN_NAMES = {
			Pattern.compile("descr: +reassign to \"([\\S ]+)\""),
			Pattern.compile("descr: +([\\S ]+)"),
			Pattern.compile("[Oo]rg-?[Nn]ame: *([\\S ]+)"),
			Pattern.compile("\\w?\\.? ?\\[Organization\\] +([\\S ]+)"),
			Pattern.compile("owner: +([\\S ]+)"),
			Pattern.compile("network:Organization[\\S]*:([\\S ]+)"),
			Pattern.compile("network:Org-Name:([\\S ]+)"),
			Pattern.compile("Organization Name +: ([\\S ]+)"),
			Pattern.compile(" *[Nn]etname: +([\\S ]+)"),
	};
	private static final Pattern[] PTN_COUNTRIES = {
			Pattern.compile("[Cc]ountry: +(\\w\\w)"),
			Pattern.compile("\\[ (\\w+) database provides .*"),		// JPNIC
			Pattern.compile("(\\w+) is not an ISP .*"),				// KRNIC
			Pattern.compile("# (\\w+) WHOIS data and services .*"),	// ARIN
			Pattern.compile("network:Country-Code:(\\w+)"),			// USA というパターンもあるので
	};

	//@Inject private Logger log;
	private static Logger log = Logger.getLogger(Whois.class.getName());

	public String parse(Pattern[] ptns, String s) {
		for (Pattern p : ptns) {
			Matcher m = p.matcher(s);
			if (m.matches()) {
				return m.group(1);
			}
		}
		return null;
	}
	public String[] parse2(String ptn, String s) {
		Pattern p = Pattern.compile(ptn);
		Matcher m = p.matcher(s);
		if (m.matches()) {
			String[] array = new String[m.groupCount()];
			for (int ix = 0; ix < m.groupCount(); ix++) {
				array[ix] = m.group(ix + 1);
			}
			return array;
		}
		return null;
	}

	/*
	 * 引数のIPアドレスを含むISPを取得する
	 */
	@Override
	public KnownListIsp get(NetAddr addr) {
		Optional<KnownListIsp> rc = this.stream()
				.filter(isp -> isp.within(addr))
				.findFirst();
		if (rc != null && rc.isPresent()) {
			return rc.get();
		}

		// json: http://wq.apnic.net/whois-search/query?searchtext=182.171.83.197

		KnownListIsp isp = search("http://whois.threet.co.jp/?key=", addr);
		if (isp.getName() == null || isp.getAddress().isEmpty()) {
			System.err.println();
			log.info("retry search. addr=" + addr);
			
			// sleep ...
			try {
				Thread.sleep(2 * 1000);
			}
			catch (InterruptedException ex) { }

			isp = search("http://lacnic.net/cgi-bin/lacnic/whois?query=", addr);

			if (isp.getName() == null || isp.getAddress().isEmpty()) {
				Set<NetAddr> addrs = isp.getAddress();
				String name = isp.getName();
				String country = isp.getCountry();

				log.warning("addr=" + addr + ", isp=[" + name + ", C=" + country + ", NET=" + addrs + "]");

				if (addrs.isEmpty() && name == null) {
					isp = null;
				}
				else if (name == null) {
					final KnownListIsp isp2 = new KnownListIsp(addr.toString(), country);
					isp.getAddress().forEach(a -> isp2.addAddress(a));
					isp = isp2;
				}
				else {
					isp.addAddress(new NetAddr(addr.toString() + "/32"));
				}
			}
		}

		if (isp != null) {
			add(isp);
		}
		return isp;
	}

	private KnownListIsp search(String site, NetAddr addr) {
		String netaddr = null;
		String name = null;
		String country = null;

		// URLを作成してGET通信を行う
		URL url = null;
		HttpURLConnection http = null;
		try  {
			url = new URL(site + addr);

			http = (HttpURLConnection)url.openConnection();
			http.setRequestMethod("GET");
			http.connect();

			// サーバーからのレスポンスを取得してパースする
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()))) {

				String s;
				while((s = reader.readLine()) != null) {
					String tmp = parse(PTN_NETADDRS, s);
					if (tmp != null) {
						netaddr = tmp;
					}
					tmp = parse(PTN_NAMES, s);
					if (tmp != null) {
						if (name == null) {
							name = tmp;
						}
						else if (name.contains("Inc.") || name.contains("INC.")
								|| name.contains("LTD.") 
								|| name.contains("Limited") 
								|| name.contains("Corporation")
								|| name.contains("Company")
								|| name.contains("Telecom")
								) {
						}
						else if (tmp.contains("Inc.") || tmp.contains("INC.")) {
							name = tmp;
						}
						else if (tmp.contains("HaNoi")
								|| tmp.contains("Bangkok")
								|| tmp.contains("Route")
								|| tmp.contains("STATIC")
								|| tmp.contains("Assign for")
								|| tmp.contains("contact ")
								) {
						}
						else if ("Paris, France".equals(tmp)
								|| "Security Gateway for Customer".equals(tmp)
								) {
						}
						else if ("PL".equals(country)
//								|| "CN".equals(country)
								|| "PH".equals(country)
								) {
							// 下位のエントリの方が記述が曖昧なので、文字の置換は行わない
						}
						else {
							name = tmp;
						}
					}
					tmp = parse(PTN_COUNTRIES, s);
					if (tmp != null) {
						country = tmp;
					}

					if (name == null) {
						String[] array = parse2("([\\S ]+) [\\w-]+ \\(NET-[\\d-]+\\) (\\d+\\.\\d+\\.\\d+\\.\\d+) - <a \\S+>(\\d+\\.\\d+\\.\\d+\\.\\d+)</a>", s);
						if (array != null) {
							name = array[0];
							netaddr = array[1] + " - " + array[2];
						}
					}
				}
			}
			catch (IOException e) {
				throw e;
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			if (url != null) {
				log.severe("url=" + url.toString());
			}
			e.printStackTrace();
			return null;
		}
		finally {
			if (http != null) {
				http.disconnect();
			}
		}

		// 複数の名称で登録されている組織名、もしくは、分かりづらい組織名の置換
		if (name == null) { }
		else if (name.contains("DOCOMO")) {
			name = "NTT DOCOMO, INC.";
		}
		else if (name.startsWith("Asahi Net")) {
			name = "Asahi Net Inc.";
		}
		else if (name.contains("GPRS/3G")) {
			name = "Realmove Company Limited.";
		}
		else if (name.startsWith("CNC Group CHINA169")) {
			name = "CNCGROUP China169 Backbone.";
		}
		else if (name.startsWith("DigitalOcean")) {
			name = "Digital Ocean, Inc.";
		}
		else if (name.startsWith("Amazon")) {
			name = "Amazon Technologies Inc.";
		}
		else if (name.startsWith("AT&T Wi-Fi Services")) {
			name = "AT&T Wi-Fi Services.";
		}
		else if (name.startsWith("Deutsche Telekom AG")) {
			name = "Deutsche Telekom AG";
		}
		else if (name.equals("route for Vodafone DSL customers")) {
			name = "Vodafone customers";
		}

		if (country == null) {
			if ("Sonic.net, Inc.".equals(name)) {
				country = "US";
			}
			else if ("HINET-NET".equals(name)) {
				country = "TW";
			}
			else {
				country = "--";
			}
		}
		else if ("JPNIC".equals(country)) {
			country = "JP";
		}
		else if ("ARIN".equals(country) || "USA".equals(country)) {
			country = "US";
		}
		else if ("KRNIC".equals(country)) {
			country = "KR";
		}

		KnownListIsp isp = new KnownListIsp(name, country);

		if (netaddr == null) {
			// アドレス範囲が取得できなかった場合は、グルーピングのキーとして呼び出し元で接続元IPアドれガスが設定される
			return isp;
		}

		String[] array = netaddr.split(" - ");
		if (array.length == 2) {
			NetAddr net = new NetAddr(addr.toString(), array[0], array[1]);
			isp.addAddress(net);
			return isp;
		}

		String[] cidrs = netaddr.split(",");
		for (String cidr : cidrs) {
			cidr = cidr.trim();
			array = cidr.split("/");
			String network = array[0];
			String netmask = array[1];

			StringBuilder sb = new StringBuilder(network);
			array = network.split("\\.");
			for (int ix = array.length; ix < 4; ix++) {
				sb.append(".0");
			}
			NetAddr net = new NetAddr(sb.toString() + "/" + netmask);
			isp.addAddress(net);
		}
		return isp;
	}

	@Override
	public KnownList load(String file) throws IOException {
		KnownList list = new TsvKnownList().load(file);
		list.forEach(value -> add(value));
		return this;
	}

	public static void main(String... argv) {

		System.setProperty("proxySet" , "true");
		System.setProperty("proxyHost", "proxy.ns-sol.co.jp");
		System.setProperty("proxyPort", "8000");

		NetAddr[] addrs = {
//				new ClientAddr(""),
				new ClientAddr("61.204.36.71"),
				new ClientAddr("202.248.61.202"),
				new ClientAddr("61.204.36.81"),
				new ClientAddr("202.248.61.202"),
				new ClientAddr("61.204.36.71"),
/*
				new ClientAddr("210.1.29.82"),
				new ClientAddr("182.232.195.22"),
				new ClientAddr("203.87.156.92"),
				new ClientAddr("182.48.105.210"),
				new ClientAddr("60.251.66.155"),
				new ClientAddr("52.90.33.223"),
				new ClientAddr("106.140.52.162"),
				new ClientAddr("210.173.87.154"),

				new ClientAddr("70.62.31.2"),
				new ClientAddr("64.134.171.160"),
				new ClientAddr("110.77.214.76"),
				new ClientAddr("101.99.14.161"),
				new ClientAddr("59.153.233.226"),
				new ClientAddr("117.4.252.36"),
				new ClientAddr("222.252.17.6"),
				new ClientAddr("122.2.36.229"),
				new ClientAddr("93.150.63.11"),
				new ClientAddr("183.82.120.86"),
				new ClientAddr("103.40.133.2"),
				new ClientAddr("79.191.82.167"),
				new ClientAddr("219.90.84.2"),
				new ClientAddr("122.2.36.229"),
*/
		};

		try {
			Whois f = new Whois();
			f.load(argv[0]);

			for (NetAddr addr : addrs) {
				KnownListIsp isp = f.get(addr);
				if (isp == null) {
					System.out.println("addr=" + addr + ", isp=null");
				}
				else {
					System.out.println("addr=" + addr + ", isp=[" + isp + ", C=" + isp.getCountry() +", NET=" + isp.getAddress() + "]");
				}
			}

			for (int ix = 1; ix < argv.length; ix++) {
				String addr = argv[ix];
				KnownListIsp isp = f.get(new ClientAddr(addr));
				if (isp == null) {
					System.out.println("addr=" + addr + ", isp=null");
				}
				else {
					System.out.println("addr=" + addr + ", isp=[" + isp + ", C=" + isp.getCountry() +", NET=" + isp.getAddress() + "]");
				}
			}
//			System.out.println("f=" + f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
