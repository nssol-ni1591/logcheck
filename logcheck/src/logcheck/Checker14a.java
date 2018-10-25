package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import logcheck.Checker14.OutWrapper;
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
 * このクラスは、出力に際して最も性能がよい実装を確認するためのクラス
 * 
 */
public class Checker14a extends AbstractChecker<UserList<UserListBean>> {

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
			UserListSite site = new UserListSite(isp);
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

	// Stream.collectを使用する場合
	@WithElaps
	private void report1(final PrintWriter out, final UserList<UserListBean> list) {
		out.println(HEADER);
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
	// String.joinを使用する場合
	@WithElaps
	private void report2(final PrintWriter out, final UserList<UserListBean> list) {
		out.println(HEADER);
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
	// String連結を使用する場合
	@WithElaps
	private void report3(final PrintWriter out, final UserList<UserListBean> list) {
		out.println(HEADER);
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
	// StringBuilderを使用する場合
	@WithElaps
	private void report4(final PrintWriter out, final UserList<UserListBean> list) {
		out.println(HEADER);
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
	// Wrapperを使用する場合
	@WithElaps
	private void report5(final PrintWriter out, final UserList<UserListBean> list) {
		Checker14 c14 = new Checker14();
		out.println(HEADER);
		userlist.values().stream()
			.forEach(user -> {
				if (user.getSites().isEmpty()) {
					out.println(c14.new OutWrapper(user));
				}
				else {
					user.getSites().forEach(site ->
						out.println(c14.new OutWrapper(user, site)));
				}
			});
	}
	// Wrapperを使用する場合
	@WithElaps
	private void report6(final PrintWriter out, final UserList<UserListBean> list) {
		Checker14 c14 = new Checker14();
		out.println(HEADER);
		userlist.values().stream()
			.flatMap(user -> 
				user.getSites().isEmpty()
					? Stream.of(c14.new OutWrapper(user))
					: user.getSites().stream().map(site -> c14.new OutWrapper(user, site))
					)
			.forEach(out::println);
	}
	// Wrapperを使用する場合：条件判断をWrapper側で行う
	@WithElaps
	private void report7(final PrintWriter out, final UserList<UserListBean> list) {
		Checker14 c14 = new Checker14();
		out.println(HEADER);
		userlist.values().stream()
			.map(user -> c14.new OutWrapper(user))
			.flatMap(OutWrapper::stream)
			.forEach(out::println);
	}
	/*
	 * 拠点ごとに接続回数を取得しているので、アドレスを出力してはいけない。
	 * アドレスを出力すると、接続回数は実際の値のアドレス数の倍になる
	 * @see logcheck.AbstractChecker#report(java.io.PrintWriter, java.lang.Object)
	 */
	@Override
	public void report(final PrintWriter out, final UserList<UserListBean> list) {
		long elaps;
		PrintWriter nul;

		nul = new PrintWriter(new StringWriter(100000));
		elaps = System.currentTimeMillis();
		report2(out, list);
		log.log(Level.INFO, "dummy__: elapse={0}ms", System.currentTimeMillis() - elaps);
		nul.close();

		nul = new PrintWriter(new StringWriter(100000));
		elaps = System.currentTimeMillis();
		report1(nul, list);
		log.log(Level.INFO, "report1: elapse={0}ms", System.currentTimeMillis() - elaps);
		nul.close();

		nul = new PrintWriter(new StringWriter(100000));
		elaps = System.currentTimeMillis();
		report1(nul, list);
		log.log(Level.INFO, "report1: elapse={0}ms", System.currentTimeMillis() - elaps);
		nul.close();

		nul = new PrintWriter(new StringWriter(100000));
		elaps = System.currentTimeMillis();
		report2(nul, list);
		log.log(Level.INFO, "report2: elapse={0}ms", System.currentTimeMillis() - elaps);
		nul.close();

		nul = new PrintWriter(new StringWriter(100000));
		elaps = System.currentTimeMillis();
		report3(nul, list);
		log.log(Level.INFO, "report3: elapse={0}ms", System.currentTimeMillis() - elaps);
		nul.close();

		nul = new PrintWriter(new StringWriter(100000));
		elaps = System.currentTimeMillis();
		report4(nul, list);
		log.log(Level.INFO, "report4: elapse={0}ms", System.currentTimeMillis() - elaps);
		nul.close();

		nul = new PrintWriter(new StringWriter(100000));
		elaps = System.currentTimeMillis();
		report5(nul, list);
		log.log(Level.INFO, "report5: elapse={0}ms", System.currentTimeMillis() - elaps);
		nul.close();

		nul = new PrintWriter(new StringWriter(100000));
		elaps = System.currentTimeMillis();
		report6(nul, list);
		log.log(Level.INFO, "report6: elapse={0}ms", System.currentTimeMillis() - elaps);
		nul.close();

		nul = new PrintWriter(new StringWriter(100000));
		elaps = System.currentTimeMillis();
		report7(nul, list);
		log.log(Level.INFO, "report7: elapse={0}ms", System.currentTimeMillis() - elaps);
		nul.close();
	}

	@Override
	public String usage(String name) {
		return String.format("usage: java %s knownlist sslindex [accesslog...]", name);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper(Checker14a.class).weld(2, argv);
		System.exit(rc);
	}

}
