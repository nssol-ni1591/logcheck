package logcheck.user.sslindex;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Logger;

import logcheck.annotations.WithElaps;
import logcheck.site.SiteList;
import logcheck.user.UserList;

//@Alternative
public class SSLIndexUserList extends HashMap<String, SSLIndexUser> implements UserList<SSLIndexUser> {

//	@Inject private Logger log;
	private static Logger log = Logger.getLogger(SSLIndexUserList.class.getName());

	private static final long serialVersionUID = 1L;

// 正規化表現ではうまく処理できないのでTSV形式ということもありsplitで処理する
//	public static String PATTERN = "(\\w)\t(\\w+)\t(\\w*)\t(\\w+)\t(\\w*)\t/C=JP/ST=TOKYO/L=CHUOU-KU/O=sdc/OU=nssol/CN=(\\w+)";
//	public static String PATTERN = "(\\w)\t(\\w+)\t(\\w+)?\t(\\w+)\t(\\w+)\t(\\S+)";

	public SSLIndexUserList() {
		super(5000);
	}

	@Override @WithElaps
	public SSLIndexUserList load(String file, SiteList sitelist) throws IOException {
		Files.lines(Paths.get(file), Charset.forName("utf-8"))
				.filter(s -> test(s))
				.map(s -> parse(s))
				.forEach(b -> {
					SSLIndexUser user = this.get(b.getUserId());
					if (user == null) {
						user = new SSLIndexUser(b.getUserId(), b.getFlag(), b.getRevoce());
						this.put(b.getUserId(), user);
					}
					// 基本的にindex.txtは時系列に並んでいるようなので、同一エントリが生じたときは更新する。で問題ないはず
//					else {
//						log.warning("(SSLインデックス): user=" + user);
//					}
				});
		return this;
	}

	private SSLIndexBean parse(String s) {
		String[] array = s.split("\t");
		String flag = array[0];
		String expire = array[1];
		String revoce = array[2];
		String serial = array[3];
		String filename = array[4];

		int pos = array[5].indexOf("/CN=");
		String userId = array[5].substring(pos + 4, array[5].length());

		return new SSLIndexBean(flag, expire, revoce, serial, filename, userId);
	}
	/*
	private SSLIndexBean parse(String s) {
		String flag = null;
		String expire = null;
		String revoce = null;
		String serial = null;
		String filename = null;
		String userId = null;

		Pattern p = Pattern.compile(PATTERN);
		Matcher m = p.matcher("   " + s);		// 1文字目が欠ける対策
		if (m.find(1)) {
			flag = m.group(1);
		}
		if (m.find(2)) {
			expire = m.group(2);
		}
		if (m.find(3)) {
			revoce = m.group(3);
		}
		if (m.find(4)) {
			serial = m.group(4);
		}
		if (m.find(5)) {
			filename = m.group(5);
		}
		if (m.find(6)) {
			userId = m.group(6);
		}
		return new SSLIndexBean(flag, expire, revoce, serial, filename, userId);
	}
	*/
	private boolean test(String s) {
		boolean rc = false;
		String[] array = s.split("\t");
		if (array.length == 6) {
			int pos = s.indexOf("/CN=");
			if (pos >= 0) {
				rc = true;
			}
		}
		/*
		Pattern p = Pattern.compile(PATTERN);
		Matcher m = p.matcher(s);
		boolean rc = m.find();
		*/
		if (!rc) {
			log.warning("(SSLインデックス): s=\"" + s.trim() + "\"");
		}
		return rc;
	}

	public static void main(String... argv) {
		System.out.println("start SSLUserList.main ...");
		SSLIndexUserList map = new SSLIndexUserList();
		try {
			map = new SSLIndexUserList().load(argv[0], null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String name : map.keySet()) {
			SSLIndexUser user = map.get(name);
			System.out.println(user);
		}
		System.out.println("SSLUserList.main ... end");
	}
}
