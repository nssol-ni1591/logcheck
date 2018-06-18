package logcheck.known.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import logcheck.known.KnownListIsp;
import logcheck.util.net.NetAddr;

public class WhoisPlainParser implements Whois {

	@Inject protected Logger log;

	private static final Pattern PATTERN = Pattern.compile("^([\\S]+): +([\\S ]+)$");

	public void init() {
		if (log == null) {
			// logのインスタンスが生成できないため
			log = Logger.getLogger(this.getClass().getName());
		}
	}

	/*
	 * 引数のIPアドレスを含むISPを取得する
	 */
	public KnownListIsp get(NetAddr addr) {
		KnownListIsp isp = search("http://whois.arin.net/rest/ip/", addr);
		log.log(Level.INFO, "addr={0}: name={1}, country={2}, net={3}", new Object[] { addr, isp.getName(), isp.getCountry(), isp.toStringNetwork() });
		return isp;
	}

	/*
	 * 引数のサイトからIPアドレスを含むISPを取得する
	 */
	public KnownListIsp search(String site, NetAddr addr) {
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
			http.setRequestProperty("Accept", "text/plain");
			http.connect();

			// サーバーからのレスポンスを取得してパースする
			Map<String, String> map = new HashMap<>();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()))) {
				String s;
				while((s = reader.readLine()) != null) {
					if (s.length() == 0 || s.startsWith("#") || s.startsWith("Comment")) {
						continue;
					}
					log.log(Level.FINE, "s={0}", s);
					Matcher m = PATTERN.matcher(s);
					if (m.matches()) {
						String key = m.group(1);
						String val = m.group(2);
						map.put(key, val);
					}
				}
			}
			String netType = map.get("NetType");
			if (netType == null || !netType.startsWith("Allocated to")) {
				netaddr = getNetaddr(map);
				name = getOrganization(map);
				country = getCountry(map);
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

	public String getNetaddr(Map<String, String> map) {
		String netaddr = map.get("NetRange");
		if (netaddr != null) {
			return netaddr;
		}
		return map.get("CIDR");
	}
	public String getOrganization(Map<String, String> map) {
		return map.get("Organization");
	}
	public String getCountry(Map<String, String> map) {
		return null;
	}

}
