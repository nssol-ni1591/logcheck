package logcheck;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.site.SiteList;
import logcheck.site.SiteListKnownIsp;
import logcheck.site.SiteListMagIsp;
import logcheck.user.UserList;
import logcheck.user.sslindex.SSLIndexSite;
import logcheck.user.sslindex.SSLIndexUser;

/*
 * ユーザの利用状況を取得する：
 * 
 * 
 */
public class Checker17 extends AbstractChecker<UserList<SSLIndexUser>> {

	@Inject private KnownList knownlist;
//	@Inject private MagList maglist;
	@Inject private SiteList sitelist;
	@Inject private UserList<SSLIndexUser> userlist;
//	@Inject private SSLUserList userlist;

	@Inject private Logger log;

	private static final Pattern AUTH_PATTERN = Pattern.compile("Certificate realm restrictions successfully passed for [\\S ]+ , with certificate 'CN=(Z\\w+), [\\S ]+'");

	public Checker17 init(String knownfile, String sslindexfile) throws Exception {
		this.knownlist.load(knownfile);
		this.sitelist.load(null);
		this.userlist.load(sslindexfile, sitelist);
		return this;
	}

	@Override
	public UserList<SSLIndexUser> call(Stream<String> stream) throws Exception {
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

					SSLIndexUser user = userlist.get(userId);
					if (user == null) {
						userErrs.add(userId);

						// ログに存在するが、SSLテーブルに存在しない場合： 不正な状態を検知することができるようにuserlistに追加する
						user = new SSLIndexUser(userId, " ");
						userlist.put(userId, user);
					}

					SSLIndexSite site = user.get(b.getAddr());
					if (site == null) {
//						MagListIsp magisp = maglist.get(b.getAddr());
						IspList magisp = sitelist.get(b.getAddr());
						if (magisp == null) {
							IspList isp = knownlist.get(b.getAddr());
							if (isp == null) {
								addrErrs.add(b.getAddr());
								return;
							}
							site = new SSLIndexSite(new SiteListKnownIsp(isp));
							user.add(site);
							site.update(b.getDate());
							log.config(String.format("user=%s, isp=%s", user, isp));
						}
						else {
							site = new SSLIndexSite(new SiteListMagIsp(magisp));
							user.add(site);
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
		System.out.println("ユーザID\t国\tISP/プロジェクトID\t拠点名\tプロジェクト削除\t拠点削除\t有効\t初回日時\t最終日時\t回数");
		userlist.values().stream()
			.forEach(user -> {
				if (user.isEmpty()) {
					System.out.println(
							new StringBuilder(user.getUserId())
							.append("\t").append("-")
							.append("\t").append("-")
							.append("\t").append("-")
							.append("\t").append("-1")
							.append("\t").append("-1")
							.append("\t").append("R".equals(user.getFlag()) ? "0" : ("V".equals(user.getFlag()) ? "1" : "-1"))
							.append("\t").append("")
							.append("\t").append("")
							.append("\t").append("0")
							);
				}
				else {
					user.stream().forEach(site -> {
						System.out.println(
								new StringBuilder(user.getUserId())
								.append("\t").append(site.getCountry())
								.append("\t").append(site.getProjId())
								.append("\t").append(site.getSiteName())
//								.append("\t").append(addr)
								.append("\t").append(site.getProjDelFlag())
								.append("\t").append(site.getSiteDelFlag())
//								.append("\t").append(user.getUserDelFlag())
								.append("\t").append("R".equals(user.getFlag()) ? "0" : ("V".equals(user.getFlag()) ? "1" : "-1"))
								.append("\t").append(site.getFirstDate())
								.append("\t").append(site.getLastDate())
								.append("\t").append(site.getCount())
								);
					});
				}
			});
	}

	public static void main(String... argv) {
		if (argv.length < 2) {
			System.err.println("usage: java logcheck.Checker14 knownlist sslindex [accesslog...]");
			System.exit(1);
		}

		int rc = 0;
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker17 application = container.instance().select(Checker17.class).get();
			/*
			Checker17 application = container.instance().select(Checker17.class, new AnnotationLiteral<UseChecker14>(){
				private static final long serialVersionUID = 1L;
			}).get();
			*/
			application.init(argv[0], argv[1]).start(argv, 2);
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
			rc = 1;
		}
		System.exit(rc);
	}
}
