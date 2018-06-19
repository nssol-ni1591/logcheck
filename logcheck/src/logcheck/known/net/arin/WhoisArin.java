package logcheck.known.net.arin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.inject.Inject;

import logcheck.known.KnownListIsp;
import logcheck.known.net.Whois;
import logcheck.known.net.WhoisUtils;
import logcheck.util.net.NetAddr;

public class WhoisArin implements Whois {

	@Inject protected Logger log;

	private static final Pattern PATTERN = Pattern.compile("^([\\S]+): +([\\S ]+)$");

	public void init() {
		if (log == null) {
			// JUnitの場合、logのインスタンスが生成できないため
			log = Logger.getLogger(this.getClass().getName());
		}
	}

	/*
	 * 引数のIPアドレスを含むISPを取得する
	 */
	public KnownListIsp get(NetAddr addr) {
		try {
		KnownListIsp isp = search("http://whois.arin.net/rest/ip/", addr);
//		log.log(Level.INFO, "addr={0}: name={1}, country={2}, net={3}", new Object[] { addr, isp.getName(), isp.getCountry(), isp.toStringNetwork() });
		return isp;
		}
		catch (IOException e) { }
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
		URL url = null;
		HttpURLConnection http = null;
		try  {
			url = new URL(site + addr + "/pft");

			http = (HttpURLConnection)url.openConnection();
			http.setRequestMethod("GET");
			http.setRequestProperty("Accept", "text/plain");
			http.connect();

			// サーバーからのレスポンスを取得してパースする
			Map<String, String> map = new HashMap<>();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()))) {
				String s;
				while((s = reader.readLine()) != null) {
					if (s.isEmpty() || s.startsWith("#") || s.startsWith("Comment")) {
						continue;
					}
					//log.log(Level.FINE, "s={0}", s);
					Matcher m = PATTERN.matcher(s);
					if (m.matches()) {
						String key = m.group(1);
						String val = m.group(2);
						if (map.containsKey(key)) {
							log.log(Level.FINE, "duplicate key={0}, org={1}, new={2}", new Object[] { key, map.get(key), val });
						}
						else {
							map.put(key, val);
						}
					}
				}
			}
			String netType = map.get("NetType");
			if (netType == null
					|| (!netType.startsWith("Allocated to"))
					&& !netType.startsWith("Early Registrations,")
					) {
				// 外部NIC参照の場合は組織名の設定は行わない
				name = getOrganization(map);
			}
			netaddr = getNetaddr(map);
			country = getCountry(map);
		}
		catch (IOException e) {
			if (url != null) {
				log.log(Level.SEVERE, "url={0}, exception={1}", new Object[] { url, e });
			}
			throw e;
		}
		finally {
			if (http != null) {
				http.disconnect();
			}
		}
		return WhoisUtils.format(addr, netaddr, name, country);
	}

	public String getNetaddr(Map<String, String> map) {
		String[] keys = { "NetRange", "CIDR" };
		Optional<String> ret = Stream.of(keys)
				.filter(key -> map.get(key) != null)
				.findFirst();
		return ret.isPresent() ? map.get(ret.get()) : null;
	}
	public String getOrganization(Map<String, String> map) {
		String[] keys = { "OrgName", "CustName", "Organization" };
		Optional<String> ret = Stream.of(keys)
				.filter(key -> map.get(key) != null)
				.map(key -> map.get(key).toString())
				.filter(s -> !s.contains("@"))
				.findFirst();
		return ret.isPresent() ? ret.get() : null;
	}
	public String getCountry(Map<String, String> map) {
		return map.get("Country");
	}

}
