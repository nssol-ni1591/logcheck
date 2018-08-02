package logcheck.known.impl.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logcheck.known.KnownListIsp;
import logcheck.known.impl.AbstractWhoisServer;
import logcheck.util.NetAddr;

public class WhoisHtmlParser extends AbstractWhoisServer {

	protected static final Pattern[] PTN_NETADDRS = {
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
	protected static final Pattern[] PTN_NAMES = {
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
	protected static final Pattern[] PTN_COUNTRIES = {
			Pattern.compile("[Cc]ountry: +(\\w\\w).*"),
//			Pattern.compile("\\[ (\\w+) database provides .*"),		// JPNIC
//			Pattern.compile("(\\w+) is not an ISP .*"),				// KRNIC
//			Pattern.compile("# (\\w+) WHOIS data and services .*"),	// ARIN
			Pattern.compile("network:Country-Code:(\\w+)"),			// USA というパターンもあるので
	};

	public WhoisHtmlParser(String url) {
		super(url);
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

	private String selectOrganization(String name, String tmp) {
		if (tmp == null) {
			// Do nothing
		}
		else if (tmp.contains("@")) {
			// mail address
			// Do nothing
		}
		else if (tmp.contains("HaNoi")
				|| tmp.contains("Hanoi")
				|| tmp.contains("Bangkok")
				|| tmp.contains("Route")
				|| tmp.contains("STATIC")
				|| tmp.contains("Assign for")
				|| tmp.contains("contact ")
				) {
			// "Hanoi"とか地名が含まれている場合は住所の可能性が大きいのでnameに置換しない
		}
		else if (name != null
				&& (name.contains("Inc.") 
				|| name.contains("INC.")
				|| name.contains("LTD.") 
				|| name.contains("Limited") 
				|| name.contains("Corp")
				|| name.contains("Company")
				|| name.contains("Telecom"))
				//&& !name.startsWith("ARTERIA Networks")
				) {
			// すでに"Inc."などを含む文字列がnameに設定されている場合はnameの変更は行わない
		}
		else {
			log.log(Level.FINE, "replace key=NAMES, exists={0}, new={1}",
					new Object[] { name, tmp });
			return tmp;
		}
		return name;
	}

	/*
	 * 引数のサイトからIPアドレスを含むISPを取得する
	 */
	public KnownListIsp search(String site, NetAddr addr) throws IOException {
		// String netaddr = null
		// String name = null
		// String country = null
		final String[] attrs = new String[5];

		// URLを作成してGET通信を行う
		URL url = new URL(site + addr);
		HttpURLConnection http = (HttpURLConnection)url.openConnection();
		http.setRequestMethod("GET");
		http.connect();

		// サーバーからのレスポンスを取得してパースする
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()))) {
			reader.lines()
				.forEach(s -> {
					String netaddr = attrs[0];
					String name = attrs[1];
					String country = attrs[2];

					// select network address
					String tmp = parse(PTN_NETADDRS, s);
					if (netaddr == null) {
						netaddr = tmp;
					}
					else if (tmp != null) {
						NetAddr addr1 = new NetAddr(netaddr);
						NetAddr addr2 = new NetAddr(tmp);
						if (addr1.within(addr2)) {
							log.log(Level.FINE, "replace key=NETADDRS, exists={0}, new={1}",
									new Object[] { netaddr, tmp });
							netaddr = tmp;
							attrs[3] = name;
							attrs[4] = country;
							name = null;
							country = null;
						}
					}

					// select organization name
					tmp = parse(PTN_NAMES, s);
					name = selectOrganization(name, tmp);

					// check country
					tmp = parse(PTN_COUNTRIES, s);
					if (tmp != null) {
						if (country != null && !country.equals(tmp)) {
							log.log(Level.FINE, "replace key=COUNTRIES, exists={0}, new={1}",
									new Object[] { country, tmp });
						}
						country = tmp;
					}

					attrs[0] = netaddr;
					attrs[1] = name;
					attrs[2] = country;
				});
			String name = attrs[1] != null ? attrs[1] : attrs[3];
			String country = attrs[2] != null ? attrs[2] : attrs[4];
			return WhoisUtils.format(addr, attrs[0], name, country);
		}
		finally {
			http.disconnect();
		}
	}

}
