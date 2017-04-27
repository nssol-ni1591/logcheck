package logcheck.sdc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logcheck.util.NetAddr;

public class SdcList extends TreeMap<String, SdcListIsp> {

	private static final long serialVersionUID = 1L;
	public static final String PATTERN = "([\\S ]+)\t(\\d+\\.\\d+\\.\\d+\\.\\d+/?[\\d\\.]*)\t([\\S ]+)";

	private SdcList() { }

	public SdcListIsp get(NetAddr addr) {
		Optional<SdcListIsp> rc = values().stream().filter(isp -> {
			return isp.getAddress().stream().filter(net -> net.within(addr)).findFirst().isPresent();
		}).findFirst();
		return rc.isPresent() ? rc.get() : null;
	}

	public static SdcList load(String file) throws IOException {
		SdcList map = new SdcList();
		Files.lines(Paths.get(file), Charset.forName("MS932"))
				.filter(SdcList::test)
				.map(SdcList::parse)
				.forEach(b -> {
					SdcListIsp isp = map.get(b.getName());
					if (isp == null) {
						isp = new SdcListIsp(b.getName(), b.getType());
						map.put(b.getName(), isp);
					}
					isp.addAddress(new NetAddr(b.getAddr()));
				});
		return map;
	}

	public static SdcListBean parse(String s) {
		String addr = null;
		String name = null;
		String type = null;

		Pattern p = Pattern.compile(PATTERN);
		Matcher m = p.matcher("   " + s);		// 1文字目が欠ける対策
		if (m.find(1)) {
			name = m.group(1);
		}
		if (m.find(2)) {
			addr = m.group(2);
		}
		if (m.find(3)) {
			type = m.group(3);
		}
		return new SdcListBean(name, addr, type);
	}
	public static boolean test(String s) {
		if (s.length() == 0) {
			return false;
		}
		if (s.startsWith("#")) {
			return false;
		}

		Pattern p = Pattern.compile(PATTERN);
		Matcher m = p.matcher(s);
		boolean rc = m.find();
		if (!rc) {
			System.err.println("WARNING(SDC): " + s);
		}
		return rc;
	}
/*
	public SdcList load() {
		SdcListIsp[] list = {
				new SdcListIsp("SDC-SCLOUD-Edge"	, "172.30.90.145"),
				new SdcListIsp("SDC-PMS-Edge"		, "172.30.90.146"),
				new SdcListIsp("SDC-REDMINE-Edge"	, "172.30.90.147"),
				new SdcListIsp("SDC-INFRA-Edge"		, "172.30.90.148"),
				new SdcListIsp("SDC-APARCH-Edge"	, "172.30.90.149"),
				new SdcListIsp("SDC-MW-Edge"		, "172.30.90.150"),
				new SdcListIsp("SDC-HP-Edge"		, "172.30.90.151"),
				new SdcListIsp("SDC-SCLOUD-Edge-Test", "172.30.90.152"),
				new SdcListIsp("SDC-Redmine-Staging Edge", "172.30.90.153"),
				new SdcListIsp("SDC-SSL-Edge"		, "172.30.90.154"),
				new SdcListIsp("SDC-SYNC-Edge"		, "172.30.90.155"),

				new SdcListIsp("SDC_SDCPM_SVC_W_BIS", "172.30.90.161"),
				new SdcListIsp("SDC_SDCPM_SVC_C_DNS", "172.30.90.162"),
				new SdcListIsp("SDC_SDCPM_SVC_C_DNS", "172.30.90.163"),
//				new SdcListBean("172.30.90.164", ""),
				new SdcListIsp("SDC_SDCPM_SVC_C_LDP", "172.30.90.165"),
				new SdcListIsp("SDC_SDCPM_SVC_C_LDP", "172.30.90.166"),
				new SdcListIsp("SDC_SDCPM_SVC_W_ADS", "172.30.90.167"),
				new SdcListIsp("SDC_SDCPM_SVC_W_ADS", "172.30.90.168"),
				new SdcListIsp("SDC_SDCPM_SVC_W_ADS", "172.30.90.169"),
				new SdcListIsp("SDC_SDCPM_SVC_W_ADM", "172.30.90.179"),

				new SdcListIsp("SSL-VPN装置(本番) VIP", "192.168.190.1"),

//				new SdcListIsp("", ""),

				new SdcListIsp("全社NW接続5DC境界"			, "172.30.20.252/255.255.255.252"	, "SLC"),
				new SdcListIsp("[2DC]SDCセグメント"				, "172.30.70.0/255.255.255.0"),
				new SdcListIsp("新日鉄軟件"					, "172.30.71.0/24"),
				new SdcListIsp("新日鉄軟件"					, "172.30.76.0/24"),	//新日鉄軟件_武漢
				new SdcListIsp("新日鉄軟件"					, "172.30.77.0/24"),	//新日鉄軟件_大連
				new SdcListIsp("新日鉄軟件"					, "172.30.78.0/24"),	//新日鉄軟件_上海鉄鋼管理
				new SdcListIsp("新日鉄軟件"					, "172.30.79.0/24"),	//新日鉄軟件_上海
				new SdcListIsp("NW運用syslogサーバ"			, "172.30.88.1"						, "SLC"),
				new SdcListIsp("NW運用syslogサーバ"			, "172.30.88.3"						, "SLC"),
				new SdcListIsp("サービスアクセス(D-クラウド_Edge)"	, "172.30.89.0/255.255.255.0"),
				new SdcListIsp("コンソールアクセス"				, "172.30.90.0/255.255.255.192"),
				new SdcListIsp("運用基盤アクセス#1"				, "172.30.90.64/255.255.255.240"),
				new SdcListIsp("インターネット接続(TRUST)"		, "172.30.90.80/255.255.255.248"	, "SLC"),
				new SdcListIsp("LB-NAT"						, "172.30.90.88/255.255.255.248"),
				new SdcListIsp("運用基盤アクセス#2"				, "172.30.90.96/255.255.255.224"),
				new SdcListIsp("負荷分散VIPプール"				, "172.30.90.128/255.255.255.240"),
				new SdcListIsp("サービスアクセス(S-クラウド公開)"		, "172.30.90.144/255.255.255.240"),
				new SdcListIsp("サービスアクセス(SDC共通基盤公開)"	, "172.30.90.160/255.255.255.240"),
				new SdcListIsp("NW運用監視セグメント"			, "172.30.157.64/26"				, "SLC"),
				new SdcListIsp("旧サービスアクセス（2DC）"			, "172.31.240.1/32"),	//S-CLOUD
				new SdcListIsp("旧サービスアクセス（2DC）"			, "172.31.240.2/32"),	//PMS
				new SdcListIsp("旧サービスアクセス（2DC）"			, "172.31.240.3/32"),	//REDMINE
				new SdcListIsp("S-クラウド_プライベート#1"			, "172.31.247.128/255.255.255.192"),
				new SdcListIsp("S-クラウド_プライベート#2"			, "172.31.247.192/255.255.255.192"),
//				new SdcListIsp("NW運用JP1監視サーバ"			, "172.30.196.34"),
				new SdcListIsp("NW運用監視セグメント"			, "172.30.196.0/24"					, "SLC"),

				new SdcListIsp("サービスアクセス(D-クラウド_2.0公開)"	, "10.224.0.0/255.255.0.0"),

				new SdcListIsp("サービスアクセス(D-クラウド_2DC暫定)"	, "172.31.235.0/255.255.255.0"),
				new SdcListIsp("サービスアクセス(D-クラウド_2DC暫定)"	, "172.31.236.0/255.255.255.0"),
				new SdcListIsp("サービスアクセス(D-クラウド_2DC暫定)"	, "172.31.237.0/255.255.255.0"),
				new SdcListIsp("サービスアクセス(D-クラウド_2DC暫定)"	, "172.31.243.0/255.255.255.0"),
				new SdcListIsp("サービスアクセス(D-クラウド_2DC暫定)"	, "172.31.244.0/255.255.255.0"),
				new SdcListIsp("サービスアクセス(D-クラウド_2DC暫定)"	, "172.31.245.0/255.255.255.0"),

				new SdcListIsp("DHC_大連ラボ"					, "192.168.70.0/24"),
				new SdcListIsp("【UD:sdc4】DC-Interconnect"	, "192.168.136.0/255.255.255.192"	, "SLC"),
				new SdcListIsp("NSSDC-VPNプール(本番)"			, "192.168.160.0/255.255.252.0"),
				new SdcListIsp("インターネット接続(DMZ本番)"		, "192.168.190.0/255.255.255.128"	, "SLC"),
				new SdcListIsp("インターネット接続(DMZ検証)"		, "192.168.190.128/255.255.255.128"	, "SLC"),
				new SdcListIsp("データ移行WAN接続"				, "192.168.191.0/255.255.255.248"	, "SLC"),
				new SdcListIsp("absonne接続5DC-5DCS間WAN(主)", "192.168.191.8/255.255.255.252"	, "SLC"),
				new SdcListIsp("absonne接続5DC-5DCS間WAN(副)", "192.168.191.12/255.255.255.252"	, "SLC"),
				new SdcListIsp("DC間接続ルータ(NSSDC-VPN)渡り"	, "192.168.191.16/255.255.255.252"	, "SLC"),
				new SdcListIsp("インターネット接続(UNTRUST)"		, "192.168.191.24/255.255.255.248"	, "SLC"),
				new SdcListIsp("全社NW接続5DC-5DCS間WAN(主)"	, "192.168.191.32/255.255.255.252"	, "SLC"),
				new SdcListIsp("全社NW接続5DC-5DCS間WAN(副)"	, "192.168.191.36/255.255.255.252"	, "SLC"),
				new SdcListIsp("LBアクセス"					, "192.168.191.40/255.255.255.248"	, "SLC"),
		};
		Arrays.stream(list).forEach(b -> {
			SdcListIsp isp = get(b.getName());
			if (isp == null) {
				put(b.getName(), b);
			}
			else {
				b.getAddress().forEach(addr -> isp.addAddress(addr));
			}
		});
		return this;
	}
	*/
}
