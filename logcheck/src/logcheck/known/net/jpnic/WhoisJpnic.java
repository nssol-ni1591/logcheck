package logcheck.known.net.jpnic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import logcheck.known.KnownListIsp;
import logcheck.known.net.Whois;
import logcheck.known.net.WhoisUtils;
import logcheck.util.net.NetAddr;

public class WhoisJpnic implements Whois {

	@Inject protected Logger log;

	protected static final Pattern[] PTN_NETADDRS = {
			Pattern.compile("a\\. \\[[\\S]*\\] +<A HREF=\"\\S+\">([\\d+\\.]+/\\d+)</A>"),
			Pattern.compile("\\[IPネットワークアドレス\\] +<A HREF=\"\\S+\">([\\d+\\.]+/\\d+)</A>"),
	};
	protected static final Pattern[] PTN_NAMES = {
			Pattern.compile("g\\. \\[[\\S]*\\] +(.*)"),
			Pattern.compile("\\[Organization\\] +(.*)"),
	};

	public void init() {
		if (log == null) {
			// JUnitの場合、logのインスタンスが生成できないため
			log = Logger.getLogger(this.getClass().getName());
		}
	}

	/*
	 * 引数のIPアドレスを含むISPを取得する
	 */
	@Override
	public KnownListIsp get(NetAddr addr) {
		try {
			return search("https://whois.nic.ad.jp/cgi-bin/whois_gw?key=", addr);
		}
		catch (IOException e) {
			log.log(Level.WARNING, e.getMessage());
		}

		try {
			Thread.sleep(10 * 1000L);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		try {
			return search("https://whois.nic.ad.jp/cgi-bin/whois_gw?key=", addr);
		}
		catch (IOException e) {
			log.log(Level.WARNING, e.getMessage());
		}
		return null;
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

		// URLを作成してGET通信を行う
		URL url = new URL(site + addr);
		HttpURLConnection http = (HttpURLConnection)url.openConnection();
		http.setRequestMethod("GET");
		http.connect();

		// サーバーからのレスポンスを取得してパースする
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream(), "windows-31j"))) {
			String s;
			while((s = reader.readLine()) != null) {
				if (s.isEmpty() || s.startsWith("-")) {
					continue;
				}
				log.log(Level.FINE, "s={0}", s);
				String tmp = parse(PTN_NETADDRS, s);
				if (tmp != null) {
					netaddr = tmp;
				}
				tmp = parse(PTN_NAMES, s);
				if (tmp != null) {
					name = tmp;
				}
			}
			return WhoisUtils.format(addr, netaddr, name, "JP");
		}
		finally {
			http.disconnect();
		}
	}

}
