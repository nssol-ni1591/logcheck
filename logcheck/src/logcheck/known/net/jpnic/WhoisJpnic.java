package logcheck.known.net.jpnic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logcheck.known.KnownListIsp;
import logcheck.known.net.Whois;
import logcheck.known.net.AbstractWhoisServer;
import logcheck.known.net.WhoisUtils;
import logcheck.util.net.NetAddr;

public class WhoisJpnic extends AbstractWhoisServer implements Whois {

	protected static final Pattern[] PTN_NETADDRS = {
			Pattern.compile("a\\. \\[[\\S]*\\] +<A HREF=\"\\S+\">([\\d+\\.]+/\\d+)</A>"),
			Pattern.compile("\\[IPネットワークアドレス\\] +<A HREF=\"\\S+\">([\\d+\\.]+/\\d+)</A>"),
	};
	protected static final Pattern[] PTN_NAMES = {
			Pattern.compile("g\\. \\[[\\S]*\\] +(.*)"),
			Pattern.compile("\\[Organization\\] +(.*)"),
	};

	public WhoisJpnic() {
		super("https://whois.nic.ad.jp/cgi-bin/whois_gw?key=");
	}

	/*
	 * 引数のIPアドレスを含むISPを取得する
	 */
	@Override
	public KnownListIsp get(NetAddr addr) {
		try {
			return search(super.url, addr);
		}
		catch (IOException e) {
			log.log(Level.WARNING, e.getMessage());
		}

		// JPNICはTimeoutになることがあるので、再度実行のためのロジックを入れておく
		try {
			Thread.sleep(10 * 1000L);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		try {
			return search(super.url, addr);
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
		// String netaddr = null
		// String name = null
		final String[] attrs = new String[2];

		// URLを作成してGET通信を行う
		URL url = new URL(site + addr);
		HttpURLConnection http = (HttpURLConnection)url.openConnection();
		http.setRequestMethod("GET");
		http.connect();

		// サーバーからのレスポンスを取得してパースする
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream(), "windows-31j"))) {
			reader.lines()
				.filter(s -> !s.isEmpty() && !s.startsWith("-"))
				.forEach(s -> {
					log.log(Level.FINE, "s={0}", s);
					String tmp = parse(PTN_NETADDRS, s);
					if (tmp != null) {
						if (attrs[0] != null) {
							log.log(Level.FINE, "duplicate key=NETADDRS, exists={1}, new={2}",
									new Object[] { attrs[0], tmp });
						}
						attrs[0] = tmp;
					}
					tmp = parse(PTN_NAMES, s);
					if (tmp != null) {
						if (attrs[1] != null) {
							log.log(Level.FINE, "duplicate key=NAMES, exists={1}, new={2}",
									new Object[] { attrs[1], tmp });
						}
						attrs[1] = tmp;
					}
				});
			return WhoisUtils.format(addr, attrs[0], attrs[1], "JP");
		}
		finally {
			http.disconnect();
		}
	}

}
