package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import logcheck.annotations.UseChecker14;
import logcheck.annotations.WithElaps;
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
				//.filter(AccessLog::test)
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

	@WithElaps
	private void report1(final PrintWriter out, final UserList<UserListBean> list) {
		// アドレスを出力してはいけない。拠点ごとに回数を取得しているのに、アドレスを出力すると、回数は実際の値のアドレス数の倍になる
		out.println("ユーザID\t国\tISP/プロジェクトID\t拠点名\tプロジェクト削除\t拠点削除\tユーザ削除\t有効\t初回日時\t最終日時\t接続回数\t失効日時\tユーザ回数");
		userlist.values().stream()
			.forEach(user -> {
				if (user.getSites().isEmpty()) {
					out.println(Stream.of(user.getUserId()
							, "-"
							, "-"
							, "-"
							, "-1"
							, "-1"
							, "-1"
							, user.getValidFlag()
							, ""
							, ""
							, "0"
							, user.getRevoce()
							,"0"
							)
							.collect(Collectors.joining("\t")));
				}
				else {
					user.getSites().forEach(site ->
						out.println(Stream.of(user.getUserId()
								, site.getCountry()
								, site.getProjId()
								, site.getSiteName()
								, site.getProjDelFlag()
								, site.getSiteDelFlag()
								, site.getUserDelFlag()
								, user.getValidFlag()
								, site.getFirstDate()
								, site.getLastDate()
								, String.valueOf(site.getCount())
								, user.getRevoce()
								, String.valueOf(user.getTotal())
								)
								.collect(Collectors.joining("\t")))
							);
				}
			});
	}
	@WithElaps
	private void report2(final PrintWriter out, final UserList<UserListBean> list) {
		// アドレスを出力してはいけない。拠点ごとに回数を取得しているのに、アドレスを出力すると、回数は実際の値のアドレス数の倍になる
		out.println("ユーザID\t国\tISP/プロジェクトID\t拠点名\tプロジェクト削除\t拠点削除\tユーザ削除\t有効\t初回日時\t最終日時\t接続回数\t失効日時\tユーザ回数");
		userlist.values().stream()
			.forEach(user -> {
				if (user.getSites().isEmpty()) {
					out.println(String.join("\t"
							, user.getUserId()
							, "-"
							, "-"
							, "-"
							, "-1"
							, "-1"
							, "-1"
							, user.getValidFlag()
							, ""
							, ""
							, "0"
							, user.getRevoce()
							,"0"
							));
				}
				else {
					user.getSites().forEach(site ->
						out.println(String.join("\t"
								, user.getUserId()
								, site.getCountry()
								, site.getProjId()
								, site.getSiteName()
								, site.getProjDelFlag()
								, site.getSiteDelFlag()
								, site.getUserDelFlag()
								, user.getValidFlag()
								, site.getFirstDate()
								, site.getLastDate()
								, String.valueOf(site.getCount())
								, user.getRevoce()
								, String.valueOf(user.getTotal())
								))
							);
				}
			});
	}
	@WithElaps
	private void report3(final PrintWriter out, final UserList<UserListBean> list) {
		// アドレスを出力してはいけない。拠点ごとに回数を取得しているのに、アドレスを出力すると、回数は実際の値のアドレス数の倍になる
		out.println("ユーザID\t国\tISP/プロジェクトID\t拠点名\tプロジェクト削除\t拠点削除\tユーザ削除\t有効\t初回日時\t最終日時\t接続回数\t失効日時\tユーザ回数");
		userlist.values().stream()
			.forEach(user -> {
				if (user.getSites().isEmpty()) {
					out.println(user.getUserId()
							+ "\t" +  "-"
							+ "\t" +  "-"
							+ "\t" +  "-"
							+ "\t" +  "-1"
							+ "\t" +  "-1"
							+ "\t" +  "-1"
							+ "\t" +  user.getValidFlag()
							+ "\t" +  ""
							+ "\t" +  ""
							+ "\t" +  "0"
							+ "\t" +  user.getRevoce()
							+ "\t" + "0"
							);
				}
				else {
					user.getSites().forEach(site ->
						out.println(user.getUserId()
								+ "\t" +  site.getCountry()
								+ "\t" +  site.getProjId()
								+ "\t" +  site.getSiteName()
								+ "\t" +  site.getProjDelFlag()
								+ "\t" +  site.getSiteDelFlag()
								+ "\t" +  site.getUserDelFlag()
								+ "\t" +  user.getValidFlag()
								+ "\t" +  site.getFirstDate()
								+ "\t" +  site.getLastDate()
								+ "\t" +  site.getCount()
								+ "\t" +  user.getRevoce()
								+ "\t" +  user.getTotal()
								)
							);
				}
			});
	}
	@WithElaps
	private void report4(final PrintWriter out, final UserList<UserListBean> list) {
		// アドレスを出力してはいけない。拠点ごとに回数を取得しているのに、アドレスを出力すると、回数は実際の値のアドレス数の倍になる
		out.println("ユーザID\t国\tISP/プロジェクトID\t拠点名\tプロジェクト削除\t拠点削除\tユーザ削除\t有効\t初回日時\t最終日時\t接続回数\t失効日時\tユーザ回数");
		userlist.values().stream()
			.forEach(user -> {
				if (user.getSites().isEmpty()) {
					out.println(new StringBuilder()
							.append("\t").append(user.getUserId())
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
						out.println(new StringBuilder()
								.append("\t").append(user.getUserId())
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
	public void report(final PrintWriter out, final UserList<UserListBean> list) {
		long elaps;
		PrintWriter nul;

		nul = new PrintWriter(new StringWriter(100000));
		elaps = System.currentTimeMillis();
		report2(out, list);
		System.err.println("dummy__: elapse=" + (System.currentTimeMillis() - elaps) + "ms");
		nul.close();

		nul = new PrintWriter(new StringWriter(100000));
		elaps = System.currentTimeMillis();
		report1(nul, list);
		System.err.println("report1: elapse=" + (System.currentTimeMillis() - elaps) + "ms");
		nul.close();

		nul = new PrintWriter(new StringWriter(100000));
		elaps = System.currentTimeMillis();
		report1(nul, list);
		System.err.println("report1: elapse=" + (System.currentTimeMillis() - elaps) + "ms");
		nul.close();

		nul = new PrintWriter(new StringWriter(100000));
		elaps = System.currentTimeMillis();
		report2(nul, list);
		System.err.println("report2: elapse=" + (System.currentTimeMillis() - elaps) + "ms");
		nul.close();

		nul = new PrintWriter(new StringWriter(100000));
		elaps = System.currentTimeMillis();
		report3(nul, list);
		System.err.println("report3: elapse=" + (System.currentTimeMillis() - elaps) + "ms");
		nul.close();

		nul = new PrintWriter(new StringWriter(100000));
		elaps = System.currentTimeMillis();
		report4(nul, list);
		System.err.println("report4: elapse=" + (System.currentTimeMillis() - elaps) + "ms");
		nul.close();
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
}
