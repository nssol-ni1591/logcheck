package logcheck.known.impl.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logcheck.known.KnownListIsp;
import logcheck.util.DB;
import logcheck.util.NetAddr;

public class WhoisUtils {

	private static final Map<String, String> sites = new HashMap<>();
	private static final List<String> skip = new ArrayList<>();

	static {
		// load sites.properties
		final Logger log = Logger.getLogger(WhoisUtils.class.getName());
		final Pattern p = Pattern.compile("\"([\\S ]+)\"\\s*=\\s*\"([\\S ]+)\"");

		try (InputStream in = DB.class.getResourceAsStream("/META-INF/sites.properties")) {
			new BufferedReader(new InputStreamReader(in, Charset.forName("MS932")))
			.lines()
			.filter(s -> !s.startsWith("#"))
			.forEach(s -> {
				Matcher m = p.matcher(s);
				if (m.matches()) {
					String key = m.group(1);
					String value = m.group(2);
					sites.put(key, value);
				}
				else {
					log.log(Level.WARNING, "line={0}", s);
				}
			});
			log.log(Level.FINE, "map={0}", sites);
		}
		catch (IOException ex) {
			log.log(Level.WARNING, ex.getMessage(), ex);
		}

		// load skip
		try (InputStream is = DB.class.getResourceAsStream("/META-INF/skip.properties")) {
			final Properties props = new Properties();
			if (is != null) {
				props.load(new InputStreamReader(is));
			}
			props.values().stream()
				.map(Object::toString)
				.map(String::toLowerCase)
				.forEach(skip::add);
		}
		catch (IOException ex) {
			log.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	private WhoisUtils() { }

	/*
	 * 複数の名称で登録されている組織名、もしくは、分かりづらい組織名の置換
	 * Pivotテーブルを作成した時のグルーピングはISP名称で行われる（場合もある）ので
	 */
	public static KnownListIsp format(NetAddr addr, String netaddr, String ispname, String country) {
		String name = ispname;
		if (name != null) {
			// nameの編集処理を開始する

			// 一致するものから、含むものに変更する
			final String n = name.toLowerCase();
			Optional<String> rc = sites.keySet().stream()
					.filter(s -> n.contains(s.toLowerCase()))
					.map(sites::get)
					.findFirst();
			if (rc.isPresent()) {
				name = rc.get();
			}

			// サイトIDが設定されている場合が多いので、文字の最後が"xxx(...)"の場合、"(...)"を削除する
			int len = name.length();
			if (name.charAt(len - 1) == ')') {
				len = name.indexOf('(');
				if (len <= 0) {
					// Do nothing
				}
				else if (name.charAt(len - 1) == ' ') {
					name = name.substring(0, len - 1);
				}
				else {
					name = name.substring(0, len);
				}
			}
		}

		// Ispを生成する
		KnownListIsp isp = new KnownListIsp(name, country);
		if (netaddr != null) {
			// 複数のアドレス範囲が指定されている場合の対処："x.x.x.x/x,y.y.y.y.y/y ..."
			String[] cidrs = netaddr.split(",");
			for (String cidr : cidrs) {
				NetAddr net = new NetAddr(addr.toString(), cidr.trim());
				isp.addAddress(net);
			}
		}
		return isp;
	}
	
	// 指定されたISP名がJPNICで検索する必要があるかを確認する
	public static boolean isSkipJpnic(String name) {
		return skip.stream().anyMatch(name.toLowerCase()::equals);
	}
}
