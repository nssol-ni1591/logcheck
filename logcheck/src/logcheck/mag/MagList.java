package logcheck.mag;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logcheck.util.NetAddr;

public class MagList extends HashMap<String, MagListIsp> {

	private static final long serialVersionUID = 1L;
	//public static String PATTERN = "(PRJ_[\\w_]+)\t(^\t+)\t(^\t+)\t([\\d\\.]+)\t([\\d\\.]+)\t([\\d\\.]+)";
	//public static String PATTERN = "(PRJ_[\\w_]+)\t(.+)\t(.+)\t(.+)\t([\\d+\\.\\d+\\.\\d+\\.\\d+]+)\t([\\d+\\.\\d+\\.\\d+\\.\\d+]+)";
	//public static String PATTERN = "(PRJ_[\\w_]+)\t(.+)\t(.+)\t([\\d+\\\\.\\d+\\\\.\\d+\\\\.\\d+]+)\t([\\d+\\\\.\\d+\\\\.\\d+\\\\.\\d+]+)\t([\\d+\\\\.\\d+\\\\.\\d+\\\\.\\d+]+)";
	public static String PATTERN = "(PRJ_[\\w_]+)\t(.+)\t(.+)\t([\\d\\.～\\/]+)\t([\\d+\\.\\d+\\.\\d+\\.\\d+]+)\t([\\d+\\.\\d+\\.\\d+\\.\\d+]+)";

	private MagList() { }
	/*
	public HashMap<NetAddr, String> listAddress() {
		HashMap<NetAddr, String> map = new HashMap<>();
		values().forEach(isp -> {
			isp.getAddress().forEach(addr -> map.put(addr, isp.getName()));
		});
		return map;
	}
	*/
	/*
	 * 引数のIPアドレスを含むCompanyを取得する
	 */
	public MagListIsp get(NetAddr addr) {
		Optional<MagListIsp> rc = values().stream().filter(isp -> {
			return isp.getAddress().stream().filter(net -> net.within(addr)).findFirst().isPresent();
		}).findFirst();
		return rc.isPresent() ? rc.get() : null;
	}

	public static MagList load(String file) throws IOException {
		MagList map = new MagList();
		Files.lines(Paths.get(file), Charset.forName("MS932"))
				.filter(MagList::test)
				.map(MagList::parse)
				.forEach(b -> {
					MagListIsp mp = map.get(b.getPrjId());
					if (mp == null) {
						mp = new MagListIsp(b.getPrjId());
						map.put(b.getPrjId(), mp);
					}
					NetAddr addr = new NetAddr(b.getMagIp() + "/" + b.getMagMask());
					mp.addAddress(addr);
					//System.out.printf("prjId=%s, addr=%s\n", b.getPrjId(), addr);
				});
		return map;
	}
	public static MagListBean parse(String s) {
		String prjId = null;
		String prjName = null;
		String prjConn = null;
		String prjIp = null;
		String magIp = null;
		String magMask = null;

		String[] array = s.split("\t");
		if (array.length > 1) {
			prjId = array[1];
		}
		if (array.length > 2) {
			prjName = array[2];
		}
		if (array.length > 3) {
			prjConn = array[3];
		}
		if (array.length > 4) {
			prjIp = array[4];
		}
		if (array.length > 5) {
			magIp = array[5];
		}
		if (array.length > 6) {
			magMask = array[6];
		}
		return new MagListBean(prjId, prjName, prjConn, prjIp, magIp, magMask);
	}
	/*
	public static MagListBean parse(String s) {
		String prjId = null;
		String prjName = null;
		String prjConn = null;
		String prjIp = null;
		String magIp = null;
		String magMask = null;

		Pattern p = Pattern.compile(MagList.PATTERN);
		Matcher m = p.matcher("   " + s);		// 1文字目が欠ける対策
		if (m.find(1)) {
			prjId = m.group(1);
		}
		if (m.find(2)) {
			prjName = m.group(2);
		}
		if (m.find(3)) {
			prjConn = m.group(3);
		}
		if (m.find(4)) {
			prjIp = m.group(4);
		}
		if (m.find(5)) {
			magIp = m.group(5);
		}
		if (m.find(6)) {
			magMask = m.group(6);
		}
		return new MagListBean(prjId, prjName, prjConn, prjIp, magIp, magMask);
	}
	*/
	public static boolean test(String s) {
		if (s.startsWith("#")) {
			return false;
		}
		if (s.startsWith("\t\t")) {
			return false;
		}

		Pattern p = Pattern.compile(PATTERN);
		Matcher m = p.matcher(s);
		boolean rc = m.find();
		if (!rc) {
			System.err.println("ERROR: " + s.trim());
		}

		String[] array = s.split("\t");
		if (array.length > 5 && "非固定".equals(array[5])) {
			rc = false;
		}
		if (array.length > 6 && "非固定".equals(array[6])) {
			rc = false;
		}
		return rc;
	}

	public static void main(String... argv) {
		System.out.println("start MagList.main ...");
		MagList map = new MagList();
		try {
			map = MagList.load(argv[0]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String name : map.keySet()) {
			MagListIsp c = map.get(name);
			System.out.println(name + "=" + c.getAddress());
		}
		System.out.println("MagList.main ... end");
	}
}
