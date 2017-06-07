package logcheck;

import java.util.Map;
import java.util.TreeMap;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

/*
 * 未利用ユーザ検索：
 * 
 */
public class Checker15 extends Checker14 {

//	@Inject private Logger log;

	@Override
	public void report() {
		// 出力用コレクションに作り直す
		Map<String, Map<String, String>> projmap = new TreeMap<>(); 
		userlist.values().stream()
//				.filter(user -> user.sumCount() == 0 && "0".equals(user.getUserDelFlag()))
				.filter(user -> user.getSites().stream().mapToInt(site -> site.getCount()).sum() == 0 && "0".equals(user.getUserDelFlag()))
				.forEach(user -> {
					user.getSites().stream()
							.filter(site -> "0".equals(site.getProjDelFlag()) && "0".equals(site.getSiteDelFlag()))
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
		
		System.out.println("ユーザID\tISP/プロジェクトID\t拠点名");
		projmap.forEach((projId, sitemap) -> {
			sitemap.forEach((sitename, userId) -> {
				System.out.println(
						new StringBuilder(userId)
						.append("\t").append(projId)
						.append("\t").append(sitename)
						);
			});
		});
	}

	public static void main(String... argv) {
		if (argv.length < 1) {
			System.err.println("usage: java logcheck.Checker15 knownlist [accesslog...]");
			System.exit(1);
		}

		int rc = 0;
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker15 application = container.instance().select(Checker15.class).get();
			application.init(argv[0]).start(argv, 1);
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
			rc = 1;
		}
		System.exit(rc);
	}
}
