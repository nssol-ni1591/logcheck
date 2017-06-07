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
import logcheck.mag.MagList;
import logcheck.mag.MagListIsp;
import logcheck.user.UserList;
import logcheck.user.UserListBean;
import logcheck.user.UserListSummary;

/*
 * ユーザの利用状況を取得する：
 * 
 * 
 */
@UseChecker14
public class Checker14 extends AbstractChecker<UserList<UserListSummary>> {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;
	@Inject protected UserList<UserListSummary> userlist;

	@Inject private Logger log;

	private static final Pattern AUTH_PATTERN = Pattern.compile("Certificate realm restrictions successfully passed for [\\S ]+ , with certificate 'CN=(Z\\w+), [\\S ]+'");

	public Checker14 init(String knownfile) throws Exception {
		this.knownlist.load(knownfile);
		this.maglist.load();
//		this.userlist.load();
		this.userlist.load(UserListSummary.class);
		return this;
	}

	@Override
	public UserList<UserListSummary> call(Stream<String> stream) throws Exception {
		stream//.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.filter(b -> AUTH_PATTERN.matcher(b.getMsg()).matches())
				.forEach(b -> {
//					NetAddr addr = b.getAddr();
					String userId = null;

					Matcher m = AUTH_PATTERN.matcher("   " + b.getMsg()); // 1文字目が欠ける対策
					if (m.find(1)) {
						userId = m.group(1);
					}

					UserListBean<UserListSummary> user = userlist.get(userId);
					if (user == null) {
//						log.warning("not found user: userid=" + userId);
						userErrs.add(userId);
						return;
					} 

					UserListSummary site = user.getSite(b.getAddr());
					if (site == null) {
						MagListIsp magisp = maglist.get(b.getAddr());
						if (magisp == null) {
							IspList isp = knownlist.get(b.getAddr());
							if (isp == null) {
//								log.warning("unknown ip: addr=" + addr);
								addrErrs.add(b.getAddr());
								return;
							}
//							user.update(b, isp);
							site = new UserListSummary(isp);
							user.addSite(site);
							site.update(b.getDate());
							log.config(String.format("user=%s, isp=%s", user, isp));
						}
						else {
//							user.update(b, magisp);
							site = new UserListSummary(magisp);
							user.addSite(site);
							site.update(b.getDate());
							log.config(String.format("user=%s, magisp=%s", user, magisp));
						}
					}
					else {
//						user.update(b, site);
						site.update(b.getDate());
					}
				});
		return userlist;
	}

	@Override
	public void report() {
		System.out.println("ユーザID\t国\tISP/プロジェクトID\t拠点名\tIPアドレス\tプロジェクト削除\t拠点削除\tユーザ削除\t有効\t初回日時\t最終日時\t回数");
		userlist.values().stream()
			.forEach(user -> {
				user.getSites().forEach(site -> {
					site.getAddress().forEach(addr -> {
						System.out.println(
								new StringBuilder(user.getUserId())
								.append("\t").append(site.getCountry())
								.append("\t").append(site.getProjId())
								.append("\t").append(site.getSiteName())
								.append("\t").append(addr)
								.append("\t").append(site.getProjDelFlag())
								.append("\t").append(site.getSiteDelFlag())
								.append("\t").append(user.getUserDelFlag())
								.append("\t").append(user.getValidFlag())
								.append("\t").append(site.getFirstDate())
								.append("\t").append(site.getLastDate())
								.append("\t").append(site.getCount())
								);
					});
			});
		});
	}

	public static void main(String... argv) {
		if (argv.length < 1) {
			System.err.println("usage: java logcheck.Checker14 knownlist [accesslog...]");
			System.exit(1);
		}

		int rc = 0;
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
//			Checker14 application = container.instance().select(Checker14.class).get();
			Checker14 application = container.instance().select(Checker14.class, new AnnotationLiteral<UseChecker14>(){
				private static final long serialVersionUID = 1L;
			}).get();
			application.init(argv[0]).start(argv, 1);
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
			rc = 1;
		}
		System.exit(rc);
	}
}
