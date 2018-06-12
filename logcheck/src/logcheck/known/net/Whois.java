package logcheck.known.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import logcheck.known.KnownList;
import logcheck.known.KnownListIsp;
import logcheck.known.tsv.TsvKnownList;
import logcheck.util.net.NetAddr;

@Alternative
public class Whois extends LinkedHashSet<KnownListIsp> implements KnownList {

	@Inject private Logger log;
	private static final long serialVersionUID = 1L;

	private static final Pattern[] PTN_NETADDRS = {
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
	private static final Pattern[] PTN_NAMES = {
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
	private static final Pattern[] PTN_COUNTRIES = {
			Pattern.compile("[Cc]ountry: +(\\w\\w).*"),
			Pattern.compile("\\[ (\\w+) database provides .*"),		// JPNIC
			Pattern.compile("(\\w+) is not an ISP .*"),				// KRNIC
			Pattern.compile("# (\\w+) WHOIS data and services .*"),	// ARIN
			Pattern.compile("network:Country-Code:(\\w+)"),			// USA というパターンもあるので
	};

	public String parse(Pattern[] ptns, String s) {
		for (Pattern p : ptns) {
			Matcher m = p.matcher(s);
			if (m.matches()) {
				return m.group(1);
			}
		}
		return null;
	}
	public String[] parse2(String ptn, String s) {
		Pattern p = Pattern.compile(ptn);
		Matcher m = p.matcher(s);
		if (m.matches()) {
			String[] array = new String[m.groupCount()];
			for (int ix = 0; ix < m.groupCount(); ix++) {
				array[ix] = m.group(ix + 1);
			}
			return array;
		}
		return null;
	}

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

		KnownListIsp isp = search("http://whois.threet.co.jp/?key=", addr);
		if (isp == null || isp.getName() == null || isp.getAddress().isEmpty()) {
			System.err.println();
			log.log(Level.INFO, "retry search. addr={0}", addr);

			isp = search("http://lacnic.net/cgi-bin/lacnic/whois?query=", addr);
			if (isp == null) {
				log.log(Level.WARNING, "(既知ISP_IPアドレス):addr={0}, isp=null", addr);

				// 何らかの問題で取得に失敗していてもアクセスし続けるため
				isp = new KnownListIsp(addr.toString(), "-");
				isp.addAddress(new NetAddr(addr.toString() + "/32"));
			}
			else if (isp.getName() == null || isp.getAddress().isEmpty()) {
				Set<NetAddr> addrs = isp.getAddress();
				String name = isp.getName();
				String country = isp.getCountry();

				log.log(Level.WARNING, "(既知ISP_IPアドレス):addr={0}, isp=[{1}, C={2}, NET={3}]", new Object[] { addr, name, country, addrs});

				if (addrs.isEmpty() && name == null) {
					isp = new KnownListIsp(addr.toString(), country == null ? "-" : country);
					isp.addAddress(new NetAddr(addr.toString() + "/32"));
				}
				else if (name == null) {
					final KnownListIsp isp2 = new KnownListIsp(addr.toString(), country);
					isp.getAddress().forEach(isp2::addAddress);
					isp = isp2;
				}
				else {
					isp.addAddress(new NetAddr(addr.toString() + "/32"));
				}
			}
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
					String tmp = parse(PTN_NETADDRS, s);
					if (tmp != null) {
						netaddr = tmp;
					}
					tmp = parse(PTN_NAMES, s);
					if (tmp != null) {
						if (name == null) {
							name = tmp;
						}
						else if (name.contains("Inc.") 
								|| name.contains("INC.")
								|| name.contains("LTD.") 
								|| name.contains("Limited") 
								|| name.contains("Corporation")
								|| name.contains("Company")
								|| name.contains("Telecom")
								) {
							// すでに"Inc."などを含む文字列がnameに設定されている場合はnameの変更は行わない
						}
						else if (tmp.contains("Inc.") || tmp.contains("INC.")) {
							name = tmp;
						}
						else if (tmp.contains("HaNoi") || tmp.contains("Hanoi")
								|| tmp.contains("Bangkok")
								|| tmp.contains("Route")
								|| tmp.contains("STATIC")
								|| tmp.contains("Assign for")
								|| tmp.contains("contact ")
								) {
							// "Hanoi"とか地名が含まれている場合は住所の可能性が大きいのでnameの文字列で置換しない
						}
						else if ("Paris, France".equals(tmp)
								|| "Security Gateway for Customer".equals(tmp)
								) {
							// 地名とか機器名の場合は置換しない
						}
						else if ("PL".equals(country)
								|| "PH".equals(country)
								) {
							// 下位のエントリの方が記述が曖昧なので、文字の置換は行わない
						}
						else {
							name = tmp;
						}
					}
					tmp = parse(PTN_COUNTRIES, s);
					if (tmp != null) {
						country = tmp;
					}

					if (name == null) {
						String[] array = parse2("([\\S ]+) [\\w-]+ \\(NET-[\\d-]+\\) (\\d+\\.\\d+\\.\\d+\\.\\d+) - <a \\S+>(\\d+\\.\\d+\\.\\d+\\.\\d+)</a>", s);
						if (array != null) {
							name = array[0];
							netaddr = array[1] + " - " + array[2];
						}
					}
				}
			}
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
		}
		return WhoisUtils.createKnownListIsp(addr, netaddr, name, country);
	}

	@Override
	public KnownList load(String file) throws IOException {
		KnownList list = new TsvKnownList().load(file);
		list.forEach(this::add);
		return this;
	}

}
