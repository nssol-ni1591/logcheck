package logcheck.known;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logcheck.util.NetAddr;

public class KnownList extends HashMap<String, KnownListIsp> {

	private static final long serialVersionUID = 1L;
	public static final String PATTERN = "(\\d+\\.\\d+\\.\\d+\\.\\d+/?\\d*)\t([^\t]+)\t(プライベート|\\S\\S)";

	private KnownList() { }

	/*
	 * 引数のIPアドレスを含むISPを取得する
	 */
	public KnownListIsp get(NetAddr addr) {
		Optional<KnownListIsp> rc = values().stream().filter(isp -> {
			return isp.getAddress().stream().filter(net -> net.within(addr)).findFirst().isPresent();
		}).findFirst();
		return rc.isPresent() ? rc.get() : null;
	}

	public KnownList load(String file) throws IOException {
//		KnownList map = new KnownList();
		Files.lines(Paths.get(file), Charset.forName("MS932"))
				.filter(KnownList::test)
				.map(KnownList::parse)
				.forEach(b -> {
//					KnownListIsp isp = map.get(b.getName());
					KnownListIsp isp = get(b.getName());
					if (isp == null) {
						isp = new KnownListIsp(b.getName(), b.getCountry());
//						map.put(b.getName(), isp);
						put(b.getName(), isp);
					}
					isp.addAddress(new NetAddr(b.getAddr()));
				});
//		return map;
		return this;
	}

	private static KnownListBean parse(String s) {
		String addr = null;
		String name = null;
		String country = null;

		Pattern p = Pattern.compile(KnownList.PATTERN);
		Matcher m = p.matcher("   " + s);		// 1文字目が欠ける対策
		if (m.find(1)) {
			addr = m.group(1);
		}
		if (m.find(2)) {
			name = m.group(2);
		}
		if (m.find(3)) {
			country = m.group(3);
		}
		return new KnownListBean(addr, name, country);
	}
	private static boolean test(String s) {
		if (s.startsWith("#")) {
			return false;
		}

		Pattern p = Pattern.compile(PATTERN);
		Matcher m = p.matcher(s);
		boolean rc = m.find();
		if (!rc) {
			System.err.println("WARNING(KNOWN): " + s);
		}
		return rc;
	}

	public static void main(String... argv) {
		System.out.println("start IspList.main ...");
		KnownList map = new KnownList();
		try {
			map = new KnownList().load(argv[0]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String name : map.keySet()) {
			KnownListIsp n = map.get(name);
			System.out.println(n.getCountry() + "\t" + n + "\t" + n.getAddress());
			System.out.print("\t");
			n.getAddress().forEach(s -> System.out.printf("[%s]", s.toStringRange()));
			System.out.println();
		}
		System.out.println("IspList.main ... end");
	}
}
