package logcheck.known.net.html;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import logcheck.known.KnownListIsp;
import logcheck.known.net.WhoisUtils;
import logcheck.util.net.NetAddr;

public abstract class WhoisHtmlParser {

	@Inject protected Logger log;

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

	public void init() {
		if (log == null) {
			// JUnitの場合、logのインスタンスが生成できないため
			log = Logger.getLogger(this.getClass().getName());
		}
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

	/*
	 * 引数のサイトからIPアドレスを含むISPを取得する
	 */
	public KnownListIsp search(String site, NetAddr addr) throws IOException {
		String netaddr = null;
		String name = null;
		String country = null;

		// URLを作成してGET通信を行う
		URL url = new URL(site + addr);
		HttpURLConnection http = (HttpURLConnection)url.openConnection();
		http.setRequestMethod("GET");
		http.connect();

		// サーバーからのレスポンスを取得してパースする
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()))) {

			String s;
			while((s = reader.readLine()) != null) {
				// check network address
				String tmp = parse(PTN_NETADDRS, s);
				if (tmp != null) {
					netaddr = tmp;
				}

				// check organization name
				tmp = parse(PTN_NAMES, s);
				if (tmp == null) {
					// Do nothing
				}
				else if (tmp.contains("@")) {
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
						|| name.contains("Telecom")
						)) {
					// すでに"Inc."などを含む文字列がnameに設定されている場合はnameの変更は行わない
				}
				else {
					name = tmp;
				}

				// check country
				tmp = parse(PTN_COUNTRIES, s);
				if (tmp != null) {
					country = tmp;
				}
			}
			return WhoisUtils.format(addr, netaddr, name, country);
		}
		finally {
			http.disconnect();
		}
	}

}
