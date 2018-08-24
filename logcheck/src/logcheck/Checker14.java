package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import logcheck.annotations.UseChecker14;
import logcheck.known.KnownList;
import logcheck.known.KnownListIsp;
import logcheck.log.AccessLog;
import logcheck.log.AccessLogBean;
import logcheck.site.SiteList;
import logcheck.site.SiteListIsp;
import logcheck.site.SiteListIspImpl;
import logcheck.user.UserList;
import logcheck.user.UserListBean;
import logcheck.user.UserListSite;
import logcheck.util.WeldWrapper;

/*
 * ユーザの利用状況を取得する：
 * 
 * 
 */
@UseChecker14
public class Checker14 extends AbstractChecker<UserList<UserListBean>> {

	@Inject private KnownList knownlist;
	@Inject private SiteList sitelist;
	@Inject protected UserList<UserListBean> userlist;

	@Inject private Logger log;

	private static final String HEADER =
			String.join("\t"
					, "ユーザID"
					, "国"
					, "ISP/プロジェクトID"
					, "拠点名"
					, "プロジェクト削除"
					, "拠点削除"
					, "ユーザ削除"
					, "有効"
					, "初回日時"
					, "最終日時"
					, "接続回数"
					, "失効日時"
					, "ユーザ回数");

	public void init(String...argv) throws IOException, ClassNotFoundException, SQLException {
		this.knownlist.load(argv[0]);
		this.sitelist.load(null);
		this.userlist.load(argv[1], sitelist);
	}

	private void sub(AccessLogBean b, UserListBean user) {
		SiteListIsp magisp = sitelist.get(b.getAddr());
		if (magisp == null) {
			KnownListIsp isp = knownlist.get(b.getAddr());
			// knownlist.get(...)はnullを返却しない
			UserListSite site = new UserListSite(isp/*, "0"*/);
			user.addSite(site);
			site.update(b.getDate());
			//Invoke method(s) only conditionally.
			String msg = String.format("user=%s, isp=%s", user, isp);
			log.fine(msg);
		}
		else {
			UserListSite site;
			// プロジェクトIDはRoleから取得する
			// ただ複数あるパターンが一般的で、1つ目は大体「NSSDC Common Role」なので、2つ以上ある場合は2つ目をプロジェクトIDとする
			String[] roles = b.getRoles();
			if (roles.length == 0) {
				site = new UserListSite(new SiteListIspImpl(magisp, "--"), "-1", "");
			}
			else if (roles.length == 1) {
				site = new UserListSite(new SiteListIspImpl(magisp, b.getRoles()[0]), "-1", "");
			}
			else {
				site = new UserListSite(new SiteListIspImpl(magisp, b.getRoles()[1]), "-1", "");
			}
			site.addAddress(b.getAddr());
			user.addSite(site);
			site.update(b.getDate());
			//Invoke method(s) only conditionally.
			String msg = String.format("user=%s, magisp=%s", user, magisp);
			log.fine(msg);
		}
	}

	@Override
	public UserList<UserListBean> call(Stream<String> stream) {
		stream//.parallel()		// parallel()を使用するとOutOfMemory例外が発生する　=> なぜ?
				.map(AccessLog::parse)
				.filter(Objects::nonNull)
				.filter(b -> Stream.of(SESS_START_PATTERN)
						// 正規化表現に一致するメッセージのみを処理対象にする
						.anyMatch(p -> p.matcher(b.getMsg()).matches())
						)
				.filter(b -> b.getId().startsWith("Z"))
				.forEach(b -> {
					String userId = null;
					userId = b.getId();

					UserListBean user = userlist.get(userId);
					if (user == null) {
						// ユーザIDに全角英数字で入力する人がいる
						user = userlist.get(Normalizer.normalize(userId, Normalizer.Form.NFKC));
						if (user == null) {
							userErrs.add(userId);

							// ログに存在するがリストに存在しない場合： 不正な状態を検知することができるようにuserlistに追加する
							user = new UserListBean(userId, "-1");
							userlist.put(userId, user);
						}
					}

					UserListSite site = user.getSite(b.getAddr());
					if (site == null) {
						sub(b, user);
					}
					else {
						site.update(b.getDate());
					}
				});
		return userlist;
	}

	/*
	 * 拠点ごとに接続回数を取得しているので、アドレスを出力してはいけない。
	 * アドレスを出力すると、接続回数は実際の値のアドレス数の倍になる
	 * @see logcheck.AbstractChecker#report(java.io.PrintWriter, java.lang.Object)
	 */
	@Override
	public void report(final PrintWriter out, final UserList<UserListBean> list) {
		out.println(HEADER);
		userlist.values().stream()
			.map(OutWrapper::new)
			.flatMap(OutWrapper::stream)
			.forEach(out::println);
	}

	@Override
	public String usage(String name) {
		return String.format("usage: java %s knownlist sslindex [accesslog...]", name);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper(Checker14.class).weld(new AnnotationLiteral<UseChecker14>() {
			private static final long serialVersionUID = 1L;
		}, 2, argv);
		System.exit(rc);
	}

	public class OutWrapper {

		protected final UserListBean user;
		protected final UserListSite site;

		public OutWrapper(UserListBean user) {
			this.user = user;
			this.site = null;
		}
		public OutWrapper(UserListBean user, UserListSite site) {
			this.user = user;
			this.site = site;
		}

		String getUserId() {
			return user.getUserId();
		}
		String getCountry() {
			return site == null ? "-" : site.getCountry();
		}
		String getProjId() {
			return site == null ? "-" : site.getProjId();
		}
		String getSiteName() {
			return site == null ? "-" : site.getSiteName();
		}
		String getProjDelFlag() {
			return site == null ? "-1" : site.getProjDelFlag();
		}
		String getSiteDelFlag() {
			return site == null ? "-1" : site.getSiteDelFlag();
		}
		String getUserDelFlag() {
			return site == null ? "-1" : site.getUserDelFlag();
		}
		String getValidFlag() {
			return user.getValidFlag();
		}
		String getFirstDate() {
			return site == null ? "" : site.getFirstDate();
		}
		String getLastDate() {
			return site == null ? "" : site.getLastDate();
		}
		String getCount() {
			return site == null ? "0" : String.valueOf(site.getCount());
		}
		String getRevoce() {
			return user.getRevoce();
		}
		String getTotal() {
			return site == null ? "0" : String.valueOf(user.getTotal());
		}

		// 本来ならばstaticにしたいが、innerのため定義できない
		public Stream<OutWrapper> stream() {
			if (user.getSites().isEmpty()) {
				return Stream.of(this);
			}
			return user.getSites().stream().map(s -> new OutWrapper(user, s));
		}

		@Override
		public String toString() {
			return String.join("\t"
					, getUserId()
					, getCountry()
					, getProjId()
					, getSiteName()
					, getProjDelFlag()
					, getSiteDelFlag()
					, getUserDelFlag()
					, getValidFlag()
					, getFirstDate()
					, getLastDate()
					, getCount()
					, getRevoce()
					, getTotal());
		}
	}

}
