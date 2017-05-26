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
import logcheck.log.AccessLogBean;
import logcheck.mag.MagList;
import logcheck.mag.MagListIsp;
import logcheck.user.UserList;
import logcheck.user.UserListBean;
import logcheck.user.UserListSite;
import logcheck.util.NetAddr;

/*
 * ユーザの利用状況を取得する：
 * 
 * 
 */
public class Checker14 extends AbstractChecker<UserList> {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;
	@Inject private UserList userlist;

	@Inject private Logger log;

	private static final Pattern AUTH_PATTERN = Pattern.compile("Certificate realm restrictions successfully passed for [\\S ]+ , with certificate 'CN=(Z\\w+), [\\S ]+'");

	public Checker14 init(String knownfile) throws Exception {
		this.knownlist.load(knownfile);
		this.maglist.load();
		this.userlist.load();
		return this;
	}

	public static boolean test(AccessLogBean b) {
		// メッセージにIPアドレスなどが含まれるログは、それ以外の部分を比較対象とするための前処理
		return AUTH_PATTERN.matcher(b.getMsg()).matches();
	}

	public UserList call(Stream<String> stream) throws Exception {
		stream//.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.filter(Checker14::test)
				.forEach(b -> {
					NetAddr addr = b.getAddr();
					String userId = null;

					Matcher m = AUTH_PATTERN.matcher("   " + b.getMsg()); // 1文字目が欠ける対策
					if (m.find(1)) {
						userId = m.group(1);
					}

					UserListBean u = userlist.get(userId);
					if (u == null) {
						log.warning("not found user: userid=" + userId);
						return;
					} 

					UserListSite site = u.getSite(b.getAddr());
					if (site == null) {
						MagListIsp magisp = maglist.get(addr);
						if (magisp == null) {
							IspList isp = knownlist.get(addr);
							if (isp == null) {
								log.warning("unknown ip: addr=" + addr);
								return;
							}
							u.update(b, isp);
						}
						else {
							u.update(b, magisp);
						}
					}
					else {
						u.update(b, site);
					}
				});
		return userlist;
	}

	public void report(UserList map) {
		System.out.println("ユーザID\tプロジェクトID\t拠点名\tプロジェクト削除\t拠点削除\tユーザ削除\t有効\t初回日時\t最終日時\t回数");
		map.values().stream()
			.forEach(list -> {
				list.getSites().forEach(site -> {
					System.out.println(
							new StringBuilder(list.getUserId())
							.append("\t")
							.append(site.getProjId())
							.append("\t")
							.append(site.getSiteName())
							.append("\t")
							.append(site.getProjDelFlag())
							.append("\t")
							.append(site.getSiteDelFlag())
							.append("\t")
							.append(list.getUserDelFlag())
							.append("\t")
							.append(list.getValidFlag())
							.append("\t")
							.append(site.getFirstDate())
							.append("\t")
							.append(site.getLastDate())
							.append("\t")
							.append(site.getCount())
							);
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
			Checker14 application = container.instance().select(Checker14.class).get();
			application.init(argv[0]).start(argv, 1);
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
			rc = 1;
		}
		System.exit(rc);
	}
}