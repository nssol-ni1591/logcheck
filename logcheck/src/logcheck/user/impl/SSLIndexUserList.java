package logcheck.user.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.enterprise.inject.Alternative;

import logcheck.annotations.WithElaps;
import logcheck.site.SiteList;
import logcheck.user.UserList;
import logcheck.user.UserListBean;

@Alternative
public class SSLIndexUserList extends HashMap<String, UserListBean> implements UserList<UserListBean> {

	private static final long serialVersionUID = 1L;

	public SSLIndexUserList() {
		super(5000);
	}

	@Override @WithElaps
	public SSLIndexUserList load(String file, SiteList sitelist) throws IOException {
		try (Stream<String> input = Files.lines(Paths.get(file), Charset.forName("utf-8"))) {
			input.filter(SSLIndexUserList::test)
				.map(SSLIndexUserList::parse)
				.filter(b -> b.getUserId().startsWith("Z"))
				.forEach(b -> {
					UserListBean bean = this.get(b.getUserId());
					if (bean == null) {
						bean = new UserListBean(b);
						this.put(b.getUserId(), bean);
					}
					// 基本的にindex.txtは時系列に並んでいるようなので、同一エントリが生じたときは更新する。で問題ないはず
					else {
						bean.update(b);
					}
				});
		}
		return this;
	}


	// 正規化表現ではうまく処理できないのでTSV形式ということもありsplitで処理する
	public static SSLIndexBean parse(String s) {
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

	public static boolean test(String s) {
		boolean rc = false;
		String[] array = s.split("\t");
		if (array.length == 6) {
			int pos = s.indexOf("/CN=");
			if (pos >= 0) {
				rc = true;
			}
		}
		if (!rc) {
			Logger.getLogger(SSLIndexBean.class.getName()).log(Level.WARNING, "(SSLインデックス): s=\"{0}\"", s.trim());
		}
		return rc;
	}
}
