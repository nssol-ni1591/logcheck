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
import logcheck.known.net.WhoisUtils;
import logcheck.util.net.NetAddr;

public abstract class WhoisJpnicParser {

	@Inject protected Logger log;

	protected static final Pattern[] PTN_NETADDRS = {
			Pattern.compile("a\\. [\\S]* +<A HREF=\"\\S+\">([\\d+\\.]+/\\d+)</A>"),
	};
	protected static final Pattern[] PTN_NAMES = {
			Pattern.compile("g\\. [\\S]* +(.*)"),
	};

	public void init() {
		if (log == null) {
			// logのインスタンスが生成できないため
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
	public KnownListIsp search(String site, NetAddr addr) {
		String netaddr = null;
		String name = null;
		final String country = "JP";

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
			}
		}
		catch (IOException e) {
			if (url != null) {
				log.log(Level.SEVERE, "url={0}, exception={1}", new Object[] { url, e });
			}
//			return null;
		}
		finally {
			if (http != null) {
				http.disconnect();
			}
		}
		return WhoisUtils.format(addr, netaddr, name, country);
	}

}
