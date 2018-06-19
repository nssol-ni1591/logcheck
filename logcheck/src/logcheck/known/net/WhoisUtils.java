package logcheck.known.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logcheck.known.KnownListIsp;
import logcheck.util.DB;
import logcheck.util.net.NetAddr;

public class WhoisUtils {

	private static final Map<String, String> map = new HashMap<>();

	static {
		final Logger log = Logger.getLogger(WhoisUtils.class.getName());
		final Pattern p = Pattern.compile("\"([\\S ]+)\"=\"([\\S ]+)\"");

		try (InputStream in = DB.class.getResourceAsStream("/META-INF/sites.properties")) {
			new BufferedReader(new InputStreamReader(in, Charset.forName("MS932")))
			.lines()
			.filter(s -> !s.startsWith("#"))
			.forEach(s -> {
				Matcher m = p.matcher(s);
				if (m.matches()) {
					String key = m.group(1);
					String value = m.group(2);
					map.put(key, value);
				}
				else {
					log.log(Level.WARNING, "line={0}", s);
				}
			});
			log.log(Level.FINE, "map={0}", map);
		}
		catch (IOException ex) {
			log.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	/*
	 * 複数の名称で登録されている組織名、もしくは、分かりづらい組織名の置換
	 */
	public static KnownListIsp format(NetAddr addr, String netaddr, String name, String country) {
		if (name == null) {
			// nameがnullの場合は何もしない
		}
		else {
			String s = map.get(name);
			// nameが一致するものがあれば置換する
			if (s != null ) {
				name = s;
			}

			// サイトIDが設定されている場合が多いので、文字の最後が"xxx(...)"の場合、"(...)"を削除する
			int len = name.length();
			if (name.charAt(len - 1) == ')') {
				len = name.indexOf("(");
				if (len != 0) {
					if (name.charAt(len - 1) == ' ') {
						name = name.substring(0, len - 1);
					}
					else {
						name = name.substring(0, len);
					}
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

	// 旧ロジック
	public static KnownListIsp format2(NetAddr addr, String netaddr, String name, String country) {
		// 複数の名称で登録されている組織名、もしくは、分かりづらい組織名の置換
		if (name == null) {
			// nameがnullの場合は何もしない
		}
		else if (name.equals("MOL Infomation Systems Ltd.")) {
			// 登録ミス
			name = "MOL Information Systems Ltd.";
		}
//		else if (name.contains("DOCOMO")) {
//			name = "NTT DOCOMO, INC.";
//		}
//		else if (name.startsWith("Asahi Net")) {
//			name = "Asahi Net Inc.";
//		}
		else if (name.contains("GPRS/3G")) {
			name = "Realmove Company Limited.";
		}
//		else if (name.startsWith("CNC Group CHINA169")) {
//			name = "CNCGROUP China169 Backbone.";
//		}
//		else if (name.startsWith("DigitalOcean")) {
//			name = "Digital Ocean, Inc.";
//		}
//		else if (name.startsWith("Amazon")) {
//			name = "Amazon Technologies Inc.";
//		}
//		else if (name.startsWith("AT&T Wi-Fi Services")) {
//			name = "AT&T Wi-Fi Services.";
//		}
		else if (name.startsWith("IMPRESA 14 public subnet")) {
			name = "Fastweb SpA";
		}
		else if (name.startsWith("Abuse.intrinsec.com Site Abuse")) {
			name = "OVH SAS";
		}
		else if (name.equals("IP Pool assigned to DSL subscribers")) {
			name = "Vodafone DSL Italy";
		}
		else {
			// サイトIDが設定されている場合が多いので、文字の最後が"xxx(...)"の場合、"(...)"を削除する
			int len = name.length();
			if (name.charAt(len - 1) == ')') {
				len = name.indexOf("(");
				if (len != 0) {
					if (name.charAt(len - 1) == ' ') {
						name = name.substring(0, len - 1);
					}
					else {
						name = name.substring(0, len);
					}
				}
			}
		}

//		if (country == null) {
//			if ("Sonic.net, Inc.".equals(name)) {
//				country = "US";
//			}
//			else if ("HINET-NET".equals(name)) {
//				country = "TW";
//			}
//			else {
//				country = "--";
//			}
//		}
//		else if ("JPNIC".equals(country)) {
//			country = "JP";
//		}
//		else if ("ARIN".equals(country) || "USA".equals(country)) {
//			country = "US";
//		}
//		else if ("KRNIC".equals(country)) {
//			country = "KR";
//		}

		// 取得に失敗している場合はデフォルト値を埋め込む

//		if (netaddr == null) {
//			netaddr = addr.toString();
//		}
//		if (name == null) {
//			name = "";
//		}
//		if (country == null) {
//			country = "--";
//		}

		KnownListIsp isp = new KnownListIsp(name, country);

//		if (netaddr == null) {
//			// アドレス範囲が取得できなかった場合は、グルーピングのキーとして呼び出し元で接続元IPアドれガスが設定される
//			return isp;
//		}
/*
		String[] array = netaddr.split(" - ");
		if (array.length == 2) {
			NetAddr net = new NetAddr(addr.toString(), array[0], array[1]);
			isp.addAddress(net);
			return isp;
		}
*/
		if (netaddr != null) {
			// 複数のアドレス範囲が指定されている場合の対処："x.x.x.x/x,y.y.y.y.y/y ..."
			String[] cidrs = netaddr.split(",");
			for (String cidr : cidrs) {
				NetAddr net = new NetAddr(addr.toString(), cidr);
				isp.addAddress(net);
			}
		}
		return isp;
	}

}
