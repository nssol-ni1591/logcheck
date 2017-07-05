package logcheck.known.tsv;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.inject.Alternative;

import logcheck.annotations.WithElaps;
import logcheck.known.KnownList;
import logcheck.known.KnownListBean;
import logcheck.known.KnownListIsp;
import logcheck.util.net.NetAddr;

/*
 * 既知のISPのIPアドレスを取得する
 * 取得先は、引数に指定された「既知ISP_IPアドレス一覧」ファイル
 * 
 * 問題点：
 * 広いアドレス空間をISPが取得し、その一部を企業に貸し出しているよう場合、
 * IPアドレスから取得される接続元はISP名ではなく企業名を取得したい。
 * 今のHashMapでは、Hash地の値により、どちらが取得されるか判断付かない。
 */
@Alternative
public class TsvKnownList extends LinkedHashSet<KnownListIsp> implements KnownList {

	private static Logger log = Logger.getLogger(TsvKnownList.class.getName());
	private static final long serialVersionUID = 1L;

	public static final String PATTERN = "(\\d+\\.\\d+\\.\\d+\\.\\d+/?\\d*)\t([^\t]+)\t(プライベート|\\S\\S)";

	public TsvKnownList() {
		super(200);
	}

	/*
	 * 引数のIPアドレスを含むISPを取得する
	 */
	public KnownListIsp get(NetAddr addr) {
		Optional<KnownListIsp> rc = this.stream()
/*
				.filter(isp -> {
//					return isp.getAddress().stream().filter(net -> net.within(addr)).findFirst().isPresent();
					return isp.getAddress().stream().anyMatch(net -> net.within(addr));
				})
*/
//				.filter(isp -> isp.getAddress().stream().anyMatch(net -> net.within(addr)))
				.filter(isp -> isp.within(addr))
				.findFirst();
		return rc.isPresent() ? rc.get() : null;
	}

	@WithElaps
	public KnownList load(String file) throws IOException {
		Files.lines(Paths.get(file), Charset.forName("MS932"))
				.filter(TsvKnownList::test)
				.map(TsvKnownList::parse)
				.forEach(b -> {
//					KnownListIsp isp = get(b.getName());
					KnownListIsp isp = get(new NetAddr(b.getAddr()));
					if (isp == null) {
						isp = new KnownListIsp(b.getName(), b.getCountry());
//						put(b.getName(), isp);
//						put(new NetAddr(b.getAddr()), isp);
						add(isp);
					}
					isp.addAddress(new NetAddr(b.getAddr()));
				});
		return this;
	}

	private static KnownListBean parse(String s) {
		String addr = null;
		String name = null;
		String country = null;

		Pattern p = Pattern.compile(TsvKnownList.PATTERN);
		Matcher m = p.matcher("   " + s);		// 1文字目が欠ける対策
		if (m.find(1)) {
			addr = m.group(1);
		}
		if (m.find(2)) {
			name = m.group(2);
			if (name.length() > 0 && name.charAt(0) == '\"') {
				name = name.substring(1);
			}
			if (name.length() > 0 && name.charAt(name.length() - 1) == '\"') {
				name = name.substring(0, name.length() - 1);
			}
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
//			System.err.println("WARNING(KNOWN): " + s);
			log.warning("(既知ISP_IPアドレス): s=\"" + s + "\"");
		}
		return rc;
	}

	public static void main(String... argv) {
		System.out.println("start IspList.main ...");
		KnownList map = new TsvKnownList();
		try {
			map = new TsvKnownList().load(argv[0]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		for (String name : map.keySet()) {
//		for (NetAddr addr : map.keySet()) {
//			KnownListIsp n = map.get(addr);
		for (KnownListIsp n : map) {
			System.out.println(n.getCountry() + "\t" + n + "\t" + n.getAddress());
			System.out.print("\t");
			n.getAddress().forEach(s -> System.out.printf("[%s]", s.toStringRange()));
			System.out.println();
		}
		System.out.println("IspList.main ... end");
	}
}
