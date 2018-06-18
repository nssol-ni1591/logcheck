package logcheck.known.net.apnic;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import logcheck.known.KnownListIsp;
import logcheck.known.net.Whois;
import logcheck.known.net.WhoisUtils;
import logcheck.util.net.NetAddr;

public class WhoisApnic implements Whois {

	@Inject private Logger log;

	public void init() {
		if (log == null) {
			log = Logger.getLogger(this.getClass().getName());
		}
	}

	/*
	 * 引数のIPアドレスを含むISPを取得する
	 */
	public KnownListIsp get(NetAddr addr) {
		try {
			KnownListIsp isp = search("https://wq.apnic.net/query?searchtext=", addr);
			return isp;
		}
		catch (IOException e) { }
		return null;
	}

	public KnownListIsp search(String site, NetAddr addr) throws IOException {
		String netaddr = null;
		String name = null;
		String country = null;

		URL url = null;
		HttpURLConnection http = null;
		try  {
			url = new URL(site + addr);
			log.log(Level.FINE, "request url={0}", url);

			http = (HttpURLConnection)url.openConnection();
			http.setRequestMethod("GET");
			http.connect();

			JsonbConfig config = new JsonbConfig()
					.withFormatting(true)
					.withDeserializers(new SearchMapDeserializer());
			Jsonb jsonb = JsonbBuilder.create(config);
			Type type = new SearchMap() {
				private static final long serialVersionUID = 1L;
			}.getClass().getGenericSuperclass();

			SearchMap map = jsonb.fromJson(http.getInputStream(), type);

			netaddr = getNetaddr(map);
			name = getOrganization(map);
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

	public String getNetaddr(SearchMap map) {
		String[] keys = { "inetnum", "netrange" };
		Optional<String> ret = Stream.of(keys)
				.filter(key -> map.get(key) != null)
				.findFirst();
		return ret.isPresent() ? map.get(ret.get()).getValue() : null;
	}
	public String getOrganization(SearchMap map) {
		//String[] keys = { "org-name", "organization", "descr", "owner", "role" };
		String[] keys = { "org-name", "organization", "descr", "role", "owner" };
		// org-name for RIPE
		// owner for "CLARO S.A." (test10)
		Optional<String> ret = Stream.of(keys)
				.filter(key -> map.get(key) != null)
				.map(key -> map.get(key).getValue())
				.filter(s -> !s.contains("@"))
				.filter(s -> !s.equals("Japan Network Information Center"))	// JPNICから取得する
				.filter(s -> !s.equals("ARTERIA Networks Corporation"))		// JPNICに詳細な情報アリ
				.findFirst();
		return ret.isPresent() ? ret.get() : null;
	}
	public String getCountry(SearchMap map) {
		String[] keys = { "country" };
		Optional<String> ret = Stream.of(keys)
				.filter(key -> map.get(key) != null)
				.findFirst();
		return ret.isPresent() ? map.get(ret.get()).getValue() : null;
	}

}
