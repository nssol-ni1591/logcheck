package logcheck;

import java.io.PrintWriter;
import java.text.Normalizer;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import logcheck.annotations.UseChecker14;
import logcheck.known.KnownList;
import logcheck.known.KnownListIsp;
import logcheck.log.AccessLog;
import logcheck.site.SiteList;
import logcheck.site.SiteListIsp;
import logcheck.site.SiteListIspImpl;
import logcheck.user.UserList;
import logcheck.user.UserListBean;
import logcheck.user.UserListSite;
import logcheck.util.weld.WeldWrapper;

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
/*
	private static final Pattern AUTH_PATTERN = 
			Pattern.compile("VPN Tunneling: Session started for user with IPv4 address ([\\w\\.]+), hostname ([\\S]+)");
*/
	public void init(String...argv) throws Exception {
		this.knownlist.load(argv[0]);
		this.sitelist.load(null);
		this.userlist.load(argv[1], sitelist);
	}

	@Override
	public UserList<UserListBean> call(Stream<String> stream) throws Exception {
		stream//.parallel()		// parallel()を使用するとOutOfMemory例外が発生する　=> なぜ?
				.filter(AccessLog::test)
				.map(AccessLog::parse)
//				.filter(b -> SESS_START_PATTERN.matcher(b.getMsg()).matches())
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
						SiteListIsp magisp = sitelist.get(b.getAddr());
						if (magisp == null) {
							KnownListIsp isp = knownlist.get(b.getAddr());
							if (isp == null) {
								addrErrs.add(b.getAddr());
								return;
							}
							site = new UserListSite(isp/*, "0"*/);
							user.addSite(site);
							site.update(b.getDate());
							log.config(String.format("user=%s, isp=%s", user, isp));
						}
						else {
							if (/*b.getRoles() == null || */b.getRoles().length < 2) {
								site = new UserListSite(new SiteListIspImpl(magisp, b.getRoles()[0]), "-1", "");
							}
							else {
								site = new UserListSite(new SiteListIspImpl(magisp, b.getRoles()[1]), "-1", "");
							}
							site.addAddress(b.getAddr().toString());
							user.addSite(site);
							site.update(b.getDate());
							log.config(String.format("user=%s, magisp=%s", user, magisp));
						}
					}
					else {
						site.update(b.getDate());
					}
				});
		return userlist;
	}

	@Override
	public void report(final PrintWriter out, final UserList<UserListBean> list) {
		// アドレスを出力してはいけない。拠点ごとに回数を取得しているのに、アドレスを出力すると、回数は実際の値のアドレス数の倍になる
		out.println("ユーザID\t国\tISP/プロジェクトID\t拠点名\tプロジェクト削除\t拠点削除\tユーザ削除\t有効\t初回日時\t最終日時\t接続回数\t失効日時\tユーザ回数");
		userlist.values().stream()
			.forEach(user -> {
				if (user.getSites().isEmpty()) {
					out.println(new StringBuilder(user.getUserId())
							.append("\t").append("-")
							.append("\t").append("-")
							.append("\t").append("-")
							.append("\t").append("-1")
							.append("\t").append("-1")
							.append("\t").append("-1")
							.append("\t").append(user.getValidFlag())
							.append("\t").append("")
							.append("\t").append("")
							.append("\t").append("0")
							.append("\t").append(user.getRevoce())
							.append("\t").append("0")
							);
				}
				else {
					user.getSites().forEach(site ->
						out.println(new StringBuilder(user.getUserId())
								.append("\t").append(site.getCountry())
								.append("\t").append(site.getProjId())
								.append("\t").append(site.getSiteName())
								.append("\t").append(site.getProjDelFlag())
								.append("\t").append(site.getSiteDelFlag())
								.append("\t").append(site.getUserDelFlag())
								.append("\t").append(user.getValidFlag())
								.append("\t").append(site.getFirstDate())
								.append("\t").append(site.getLastDate())
								.append("\t").append(site.getCount())
								.append("\t").append(user.getRevoce())
								.append("\t").append(user.getTotal())
								)
					);
				}
			});
	}

	@Override
	public String usage(String name) {
		return String.format("usage: java %s knownlist sslindex [accesslog...]", name);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper<Checker14>(Checker14.class).weld(new AnnotationLiteral<UseChecker14>() {
			private static final long serialVersionUID = 1L;
		}, 2, argv);
		System.exit(rc);
	}
}
