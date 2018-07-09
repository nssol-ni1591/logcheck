package logcheck;

import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import logcheck.user.UserList;
import logcheck.user.UserListBean;
import logcheck.user.UserListSite;
import logcheck.util.weld.WeldWrapper;

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
						.mapToInt(UserListSite::getCount).sum() == 0 && "1".equals(user.getValidFlag())
						)
				.forEach(user -> 
					user.getSites().stream()
							.filter(site ->
									"0".equals(site.getProjDelFlag())
									&& "0".equals(site.getSiteDelFlag())
									)
							.forEach(site -> {
								String projId = site.getProjId();
								String siteName = site.getSiteName();

								Map<String, String> sitemap = projmap.computeIfAbsent(projId, key -> new TreeMap<>());

								String userId = sitemap.get(siteName);
								if (userId == null) {
									sitemap.put(siteName, user.getUserId());
								}
							})
				);
		
		out.println("ユーザID\tISP/プロジェクトID\t拠点名");
		projmap.forEach((projId, sitemap) -> 
			sitemap.forEach((sitename, userId) -> 
				out.println(Stream.of(userId
						, projId
						, sitename
						)
						.collect(Collectors.joining("\t")))
					)
				);
	}

	@Override
	public String usage(String name) {
		return String.format("usage: java %s knownlist sslindex [accesslog...]", name);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper(Checker15.class).weld(2, argv);
		System.exit(rc);
	}
}
