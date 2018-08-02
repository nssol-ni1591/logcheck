package logcheck.known.impl.net;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import logcheck.known.KnownListIsp;
import logcheck.known.impl.AbstractWhoisServer;
import logcheck.known.impl.Whois;
import logcheck.util.NetAddr;

public class WhoisApnic extends AbstractWhoisServer implements Whois {

	public WhoisApnic() {
		super("https://wq.apnic.net/query?searchtext=");
	}

	public KnownListIsp search(String site, NetAddr addr) throws IOException {
		String netaddr = null;
		String name = null;
		String country = null;

		URL url = new URL(site + addr);
		HttpURLConnection http = (HttpURLConnection)url.openConnection();
		http.setRequestMethod("GET");
		http.connect();

		JsonbConfig config = new JsonbConfig()
				.withFormatting(true)
				.withDeserializers(new ApnicDeserializer());
		try (Jsonb jsonb = JsonbBuilder.create(config)) {
			Type type = Map.class;
			Map<String, String> map = jsonb.fromJson(http.getInputStream(), type);

			netaddr = getNetaddr(map);
			name = getOrganization(map);
			country = getCountry(map);
		}
		catch (IOException e) {
			throw e;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
		finally {
			http.disconnect();
		}
		return WhoisUtils.format(addr, netaddr, name, country);
	}

	public String getNetaddr(Map<String, String> map) {
		String[] keys = { "inetnum", "netrange" };
		Optional<String> ret = Stream.of(keys)
				.filter(key -> map.get(key) != null)
				.findFirst();
		return ret.isPresent() ? map.get(ret.get()) : null;
	}
	public String getOrganization(Map<String, String> map) {
		String[] keys = { "org-name", "organization", "descr", "role", "owner" };
		// org-name for RIPE
		// owner for "CLARO S.A." (test10)
		Optional<String> ret = Stream.of(keys)
				.filter(key -> map.get(key) != null)
				.filter(s -> !s.contains("@"))
				.filter(s -> !s.equals("Japan Network Information Center"))	// JPNICから取得する
				.filter(s -> !s.equals("ARTERIA Networks Corporation"))		// JPNICに詳細な情報アリ
				.findFirst();
		return ret.isPresent() ? map.get(ret.get()) : null;
	}
	public String getCountry(Map<String, String> map) {
		String[] keys = { "country" };
		Optional<String> ret = Stream.of(keys)
				.filter(key -> map.get(key) != null)
				.findFirst();
		return ret.isPresent() ? map.get(ret.get()) : null;
	}

	@Override
	public String getName() {
		return "Apnic";
	}

}
