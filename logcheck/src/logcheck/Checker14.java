package logcheck;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import logcheck.annotations.UseChecker14;
import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.mag.MagListIsp;
import logcheck.site.SiteList;
import logcheck.site.SiteListIsp;
import logcheck.user.UserList;
import logcheck.user.UserListBean;
import logcheck.user.UserListSite;

/*
 * ユーザの利用状況を取得する：
 * 
 * 
 */
@UseChecker14
public class Checker14 extends AbstractChecker<UserList<UserListBean>> {

	@Inject private KnownList knownlist;
//	@Inject private MagList maglist;
	@Inject private SiteList sitelist;
	@Inject protected UserList<UserListBean> userlist;

	@Inject private Logger log;

	private static final Pattern AUTH_PATTERN = Pattern.compile("Certificate realm restrictions successfully passed for [\\S ]+ , with certificate 'CN=(Z\\w+), [\\S ]+'");

	public Checker14 init(String knownfile, String sslindex) throws Exception {
		this.knownlist.load(knownfile);
//		this.maglist.load();
		this.sitelist.load(null);
		this.userlist.load(sslindex, sitelist);
		return this;
	}

	@Override
	public UserList<UserListBean> call(Stream<String> stream) throws Exception {
		stream//.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.filter(b -> AUTH_PATTERN.matcher(b.getMsg()).matches())
				.forEach(b -> {
					String userId = null;
					Matcher m = AUTH_PATTERN.matcher("   " + b.getMsg()); // 1文字目が欠ける対策
					if (m.find(1)) {
						userId = m.group(1);
					}

					UserListBean user = userlist.get(userId);
					if (user == null) {
//						log.warning("not found user: userid=" + userId);
						userErrs.add(userId);
//						return;

						// ログに存在するが、SSLテーブルに存在しない場合： 不正な状態を検知することができるようにuserlistに追加する
						user = new UserListBean(userId, "-1", "-1");
						userlist.put(userId, user);
					}

					UserListSite site = user.getSite(b.getAddr());
					if (site == null) {
//						MagListIsp magisp = maglist.get(b.getAddr());
						IspList magisp = sitelist.get(b.getAddr());
//						SiteListIsp magisp = maglist.get(b.getAddr());
						if (magisp == null) {
							IspList isp = knownlist.get(b.getAddr());
							if (isp == null) {
								addrErrs.add(b.getAddr());
								return;
							}
							site = new UserListSite(isp);
							user.addSite(site);
							site.update(b.getDate());
							log.config(String.format("user=%s, isp=%s", user, isp));
						}
						else {
							site = new UserListSite(magisp);
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
	public void report() {
		// アドレスを出力してはいけない。拠点ごとに回数を取得しているのに、アドレスを出力すると、回数は実際の値のアドレス数の倍になる
		System.out.println("ユーザID\t国\tISP/プロジェクトID\t拠点名\tプロジェクト削除\t拠点削除\tユーザ削除\t有効\t初回日時\t最終日時\t回数\t失効日時");
		userlist.values().stream()
			.forEach(user -> {
				if (user.getSites().isEmpty()) {
					System.out.println(
							new StringBuilder(user.getUserId())
							.append("\t").append("-")
							.append("\t").append("-")
							.append("\t").append("-")
							.append("\t").append("-1")
							.append("\t").append("-1")
							.append("\t").append(user.getUserDelFlag())
							.append("\t").append(user.getValidFlag())
							.append("\t").append("")
							.append("\t").append("")
							.append("\t").append("0")
							.append("\t").append(user.getRevoce())
							);
				}
				else {
					user.getSites().forEach(site -> {
						System.out.println(
								new StringBuilder(user.getUserId())
								.append("\t").append(site.getCountry())
								.append("\t").append(site.getProjId())
								.append("\t").append(site.getSiteName())
								.append("\t").append(site.getProjDelFlag())
								.append("\t").append(site.getSiteDelFlag())
								.append("\t").append(user.getUserDelFlag())
								.append("\t").append(user.getValidFlag())
								.append("\t").append(site.getFirstDate())
								.append("\t").append(site.getLastDate())
								.append("\t").append(site.getCount())
								.append("\t").append(user.getRevoce())
								);
					});
				}
			});
	}

	public static void main(String... argv) {
// このメインは、ログ収集サーバ上でも呼ばれるため、プロキシーの設定を行ってはいけない
//		System.setProperty("proxySet" , "true");
//		System.setProperty("proxyHost", "proxy.ns-sol.co.jp");
//		System.setProperty("proxyPort", "8000");

		if (argv.length < 2) {
			System.err.println("usage: java logcheck.Checker14 knownlist sslindex [accesslog...]");
			System.exit(1);
		}

		int rc = 0;
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
//			Checker14 application = container.instance().select(Checker14.class).get();
			Checker14 application = container.instance().select(Checker14.class, new AnnotationLiteral<UseChecker14>(){
				private static final long serialVersionUID = 1L;
			}).get();
			application.init(argv[0], argv[1]).start(argv, 2);
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
			rc = 1;
		}
		System.exit(rc);
	}
}
