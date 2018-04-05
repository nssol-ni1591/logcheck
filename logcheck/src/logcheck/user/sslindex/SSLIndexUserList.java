package logcheck.user.sslindex;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
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
			input.filter(SSLIndexBean::test)
				.map(SSLIndexBean::parse)
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
	/*
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}
	*/
}
