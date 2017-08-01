package logcheck;

import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import logcheck.user.UserList;
import logcheck.user.UserListBean;

/*
 * 未利用ユーザ検索：
 * ユーザID、ISP/プロジェクト、拠点名のみを出力する
 */
public class Checker15 extends Checker14 {

	@Override
	public void report(final PrintWriter out, final UserList<UserListBean> list) {
		// 出力用コレクションに作り直す
		Map<String, Map<String, String>> projmap = new TreeMap<>(); 
		userlist.values().stream()
				.filter(user -> user.getSites().stream()
						.filter(site -> "0".equals(site.getUserDelFlag()))
						.mapToInt(site -> site.getCount()).sum() == 0
						&& "1".equals(user.getValidFlag())
						)
				.forEach(user -> {
					user.getSites().stream()
							.filter(site ->
									"0".equals(site.getProjDelFlag())
									&& "0".equals(site.getSiteDelFlag())
									)
							.forEach(site -> {
								String projId = site.getProjId();
								String siteName = site.getSiteName();

								Map<String, String> sitemap = projmap.get(projId);
								if (sitemap == null) {
									sitemap = new TreeMap<>();
									projmap.put(projId, sitemap);
								}
								String userId = sitemap.get(siteName);
								if (userId == null) {
									sitemap.put(siteName, user.getUserId());
								}
							});
				});
		
		out.println("ユーザID\tISP/プロジェクトID\t拠点名");
		projmap.forEach((projId, sitemap) -> {
			sitemap.forEach((sitename, userId) -> {
				out.println(new StringBuilder(userId)
						.append("\t").append(projId)
						.append("\t").append(sitename)
						);
			});
		});
	}

	public static void main(String... argv) {
		if (argv.length < 2) {
			System.err.println("usage: java logcheck.Checker15 knownlist sslindex [accesslog...]");
			System.exit(1);
		}

		int rc = 0;
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker15 application = container.instance().select(Checker15.class).get();
			application.init(argv[0], argv[1]).start(argv, 2);
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
			rc = 1;
		}
		System.exit(rc);
	}
}
