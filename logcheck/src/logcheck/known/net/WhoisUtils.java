package logcheck.known.net;

import logcheck.known.KnownListIsp;
import logcheck.util.net.NetAddr;

public class WhoisUtils {

	public static KnownListIsp createKnownListIsp(NetAddr addr, String netaddr, String name, String country) {
		// 複数の名称で登録されている組織名、もしくは、分かりづらい組織名の置換
		if (name == null) {
			// nameがnullの場合は何もしない
		}
		else if (name.contains("DOCOMO")) {
			name = "NTT DOCOMO, INC.";
		}
		else if (name.startsWith("Asahi Net")) {
			name = "Asahi Net Inc.";
		}
		else if (name.contains("GPRS/3G")) {
			name = "Realmove Company Limited.";
		}
		else if (name.startsWith("CNC Group CHINA169")) {
			name = "CNCGROUP China169 Backbone.";
		}
		else if (name.startsWith("DigitalOcean")) {
			name = "Digital Ocean, Inc.";
		}
		else if (name.startsWith("Amazon")) {
			name = "Amazon Technologies Inc.";
		}
		else if (name.startsWith("AT&T Wi-Fi Services")) {
			name = "AT&T Wi-Fi Services.";
		}
		else if (name.startsWith("Deutsche Telekom AG")) {
			name = "Deutsche Telekom AG";
		}
		else if (name.equals("route for Vodafone DSL customers")) {
			name = "Vodafone customers";
		}

		if (country == null) {
			if ("Sonic.net, Inc.".equals(name)) {
				country = "US";
			}
			else if ("HINET-NET".equals(name)) {
				country = "TW";
			}
			else {
				country = "--";
			}
		}
		else if ("JPNIC".equals(country)) {
			country = "JP";
		}
		else if ("ARIN".equals(country) || "USA".equals(country)) {
			country = "US";
		}
		else if ("KRNIC".equals(country)) {
			country = "KR";
		}

		KnownListIsp isp = new KnownListIsp(name, country);

		if (netaddr == null) {
			// アドレス範囲が取得できなかった場合は、グルーピングのキーとして呼び出し元で接続元IPアドれガスが設定される
			return isp;
		}

		String[] array = netaddr.split(" - ");
		if (array.length == 2) {
			NetAddr net = new NetAddr(addr.toString(), array[0], array[1]);
			isp.addAddress(net);
			return isp;
		}

		String[] cidrs = netaddr.split(",");
		for (String cidr : cidrs) {
			cidr = cidr.trim();
			array = cidr.split("/");
			String network = array[0];
			String netmask = array[1];

			StringBuilder sb = new StringBuilder(network);
			array = network.split("\\.");
			for (int ix = array.length; ix < 4; ix++) {
				sb.append(".0");
			}
			NetAddr net = new NetAddr(sb.toString() + "/" + netmask);
			isp.addAddress(net);
		}
		return isp;
	}

}
