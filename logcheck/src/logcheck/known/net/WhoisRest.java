package logcheck.known.net;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import logcheck.known.KnownList;
import logcheck.known.KnownListIsp;
import logcheck.known.net.rest.SearchMap;
import logcheck.known.net.rest.SearchMapDeserializer;
import logcheck.known.tsv.TsvKnownList;
import logcheck.util.net.NetAddr;

@Alternative
public class WhoisRest extends LinkedHashSet<KnownListIsp> implements KnownList {

	@Inject private Logger log;
	private static final long serialVersionUID = 1L;

	/*
	 * 引数のIPアドレスを含むISPを取得する
	 */
	@Override
	public KnownListIsp get(NetAddr addr) {
		Optional<KnownListIsp> rc = this.stream()
				.filter(isp -> isp.within(addr))
				.findFirst();
		if (rc.isPresent()) {
			return rc.get();
		}

		KnownListIsp isp = search("https://wq.apnic.net/query?searchtext=", addr);
		if (isp == null || isp.getName() == null || isp.getAddress().isEmpty()) {
				log.log(Level.WARNING, "(既知ISP_IPアドレス):addr={0}, isp=null", addr);
			}
			else if (isp.getName() == null || isp.getAddress().isEmpty()) {
				Set<NetAddr> addrs = isp.getAddress();
				String name = isp.getName();
				String country = isp.getCountry();

				log.log(Level.WARNING, "(既知ISP_IPアドレス):addr={0}, isp=[{1}, C={2}, NET={3}]", new Object[] { addr, name, country, addrs});
		}

		if (isp != null) {
			add(isp);
		}
		return isp;
	}

	private KnownListIsp search(String site, NetAddr addr) {
		String netaddr = null;
		String name = null;
		String country = null;

		CharArrayWriter wr = new CharArrayWriter(5 * 1024);
//		String str = null;

		// URLを作成してGET通信を行う
		URL url = null;
		HttpURLConnection http = null;
		try  {
			url = new URL(site + addr);

			http = (HttpURLConnection)url.openConnection();
			http.setRequestMethod("GET");
			http.connect();

			// サーバーからのレスポンスを取得してパースする
			/*
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()))) {
				String s;
				while((s = reader.readLine()) != null) {
					wr.append(s);
				}
				str = wr.toString();
			}
			*/
//			log.log(Level.INFO, "input data={0}", str);

			JsonbConfig config = new JsonbConfig()
					.withFormatting(true)
					.withDeserializers(new SearchMapDeserializer());
			Jsonb jsonb = JsonbBuilder.create(config);

			@SuppressWarnings("serial")
			Type type = new SearchMap() {}.getClass().getGenericSuperclass();

			SearchMap map = jsonb.fromJson(http.getInputStream(), type);
//			SearchMap map = jsonb.fromJson(str, type);
//			log.log(Level.INFO, "map={0}", map);

			netaddr = getNetaddr(map);
			name = getOrganization(map);
			country = getCountry(map);
			log.log(Level.INFO, "addr={3}: net={0}, name={1}, country={2}", new Object[] { netaddr, name, country, addr });
		}
		catch (IOException e) {
			if (url != null) {
				log.log(Level.SEVERE, "url={0}", url);
			}
			return null;
		}
		finally {
			if (http != null) {
				http.disconnect();
			}
			if (wr != null) {
				wr.close();
			}
		}
		return WhoisUtils.createKnownListIsp(addr, netaddr, name, country);
	}

	@Override
	public KnownList load(String file) throws IOException {
		KnownList list = new TsvKnownList().load(file);
		list.forEach(this::add);
		return this;
	}

	public String getNetaddr(SearchMap map) {
		String[] keys = { "inetnum", "netrange" };
		Optional<String> ret = Stream.of(keys)
				.filter(key -> map.get(key) != null)
				.findFirst();
		return ret.isPresent() ? map.get(ret.get()).getValue() : null;
	}
	public String getOrganization(SearchMap map) {
		String[] keys = { "descr", "organization", "owner" };
		Optional<String> ret = Stream.of(keys)
				.filter(key -> map.get(key) != null)
				.findFirst();
		return ret.isPresent() ? map.get(ret.get()).getValue() : null;
	}
	public String getCountry(SearchMap map) {
		String[] keys = { "country" };
		Optional<String> ret = Stream.of(keys)
				.filter(key -> map.get(key) != null)
				.findFirst();
		return ret.isPresent() ? map.get(ret.get()).getValue() : null;
	}

}
