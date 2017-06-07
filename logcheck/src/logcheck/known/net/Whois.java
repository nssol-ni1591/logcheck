package logcheck.known.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Alternative;

import logcheck.known.KnownList;
import logcheck.known.KnownListIsp;
import logcheck.util.net.ClientAddr;
import logcheck.util.net.NetAddr;

@Alternative
public class Whois extends TreeMap<NetAddr, KnownListIsp> implements KnownList {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Pattern[] PTN_NETADDRS = {
			Pattern.compile("% Information related to '(\\d+\\.\\d+\\.\\d+\\.\\d+ - \\d+\\.\\d+\\.\\d+\\.\\d+)'"),
			Pattern.compile("inetnum: +([\\d+\\.\\d+/\\d+]+)"),
			Pattern.compile("a. \\[Network Number\\] +(\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+)"),
			Pattern.compile("CIDR: +(\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+)"),
			Pattern.compile("network:IP-Network:(\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+)"),
			Pattern.compile("CIDR: +([\\d+\\./, ]+)"),
			Pattern.compile("IPv4 Address +: (\\d+\\.\\d+\\.\\d+\\.\\d+ - \\d+\\.\\d+\\.\\d+\\.\\d+) \\([/\\d]*\\)"),
//			new PatternWrapper("NetRange: +(\\d+\\.\\d+\\.\\d+\\.\\d+ - \\d+\\.\\d+\\.\\d+\\.\\d+)"),
	};
	private static final Pattern[] PTN_NAMES = {
			Pattern.compile("descr: +([\\S ]+)"),
			Pattern.compile("netname: +([\\S ]+)"),
			Pattern.compile("org-name: +([\\S ]+)"),
			Pattern.compile("OrgName: +([\\S ]+)"),
			Pattern.compile("g. \\[Organization\\] +([\\S ]+)"),
			Pattern.compile("owner: +([\\S ]+)"),
			Pattern.compile("network:Organization:([\\S ]+)"),
			Pattern.compile("network:Org-Name:([\\S ]+)"),
			Pattern.compile("Organization Name +: ([\\S ]+)"),
	};
	private static final Pattern[] PTN_COUNTRIES = {
			Pattern.compile("country: +(\\w\\w)"),
			Pattern.compile("Country: +(\\w\\w)"),
			Pattern.compile("network:Country-Code:(\\w\\w)"),
			Pattern.compile("\\[ (\\w+) database provides .*"),		// JPNIC
			Pattern.compile("(\\w+) is not an ISP .*"),				// KRNIC
			Pattern.compile("# (\\w+) WHOIS data and services .*"),	// ARIN
	};

	//@Inject private Logger log;
	private static Logger log = Logger.getLogger(Whois.class.getName());

	@PostConstruct
	public void init() {
		// 初期データの登録
//		put(new NetAddr(""), new KnownListIsp("", ""));
		put(new NetAddr("127.0.0.0/8"), new KnownListIsp("SPECIAL-IPV4-LOOPBACK-IANA-RESERVED", "プライベート"));
		put(new NetAddr("10.0.0.0/8"), new KnownListIsp("PRIVATE-ADDRESS-A_BLK-RFC1918-IANA-RESERVED", "プライベート"));
		put(new NetAddr("172.16.0.0/12"), new KnownListIsp("PRIVATE-ADDRESS-B_BLK-RFC1918-IANA-RESERVED", "プライベート"));
		put(new NetAddr("192.168.0.0/16"), new KnownListIsp("PRIVATE-ADDRESS-C_BLK-RFC1918-IANA-RESERVED", "プライベート"));
//		put(new NetAddr("66.240.192.0/18"), new KnownListIsp("CariNet, Inc.", "US"));
//		put(new NetAddr("65.192.0.0/11"), new KnownListIsp("CI Communications Services, Inc. d/b/a Verizon Business", "US"));
//		put(new NetAddr("54.221.0.0 - 54.221.255.255"), new KnownListIsp("Amazon.com, Inc.", "US"));
	}

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
		Optional<KnownListIsp> rc = this.keySet().stream()
				.filter(a -> a.within(addr))
				.map(a -> super.get(a))
				.findFirst();
		if (rc.isPresent()) {
			return rc.get();
		}

		KnownListIsp isp = search(addr);
		//put(isp.getAddress().iterator().next(), isp);
		if (isp != null) {
			for (NetAddr net : isp.getAddress()) {
				put(net, isp);
			}
		}
		return isp;
	}

	public KnownListIsp search(NetAddr addr) {
		String netaddr = null;
		String name = null;
		String country = null;

		// URLを作成してGET通信を行う
		URL url = null;
		HttpURLConnection http = null;
		try  {
			url = new URL("http://whois.threet.co.jp/?key=" + addr);

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
								|| name.contains("Corporation")
								|| name.contains("Company")
								) {
						}
						else if (tmp.contains("Inc.") || tmp.contains("INC.")) {
							name = tmp;
						}
						else if (tmp.contains("HaNoi")
								|| tmp.contains("Bangkok")
								|| tmp.contains("Route")
								|| tmp.contains("STATIC")
								) {
						}
						else if ("Paris, France".equals(tmp)
								|| "Security Gateway for Customer".equals(tmp)
								) {
						}
						else if ("PL".equals(country)
//								|| "PH".equals(country)
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

			if (netaddr == null || name == null) {
				log.info("addr=" + addr + ", isp=[" + netaddr + "," + name + "," + country + "]");
				return null;
			}
			if ("route for Vodafone DSL customers".equals(name)) {
				name = "Vodafone customers";
			}
			if (country == null) {
				if ("Sonic.net, Inc.".equals(name)) {
					country = "US";
				}
				else {
					country = "--";
				}
			}
			else if ("JPNIC".equals(country)) {
				country = "JP";
			}
			else if ("ARIN".equals(country)) {
				country = "US";
			}
			else if ("KRNIC".equals(country)) {
				country = "KR";
			}

			KnownListIsp isp = new KnownListIsp(name, country);

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
	}

	@Override
	public KnownList load(String file) throws IOException {
		return this;
	}

	public static void main(String... args) {

		System.setProperty("proxySet", "true");
		System.setProperty("proxyHost", "proxy.ns-sol.co.jp");
		System.setProperty("proxyPort", "8000");
		
		NetAddr[] addrs = {
//				new ClientAddr(""),
//				new ClientAddr(""),
//				new ClientAddr(""),
//				new ClientAddr(""),
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
		};

		Whois f = new Whois();
		for (NetAddr addr : addrs) {
			KnownListIsp isp = f.get(addr);
			System.out.println("addr=" + addr + ", isp=[" + isp + " C=" + isp.getCountry() +", NET=" + isp.getAddress() + "]");
		}
//		System.out.println("f=" + f);
	}

}
