package logcheck;

import java.io.PrintWriter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import logcheck.user.UserList;
import logcheck.user.UserListBean;
import logcheck.util.WeldWrapper;

/*
 * ユーザの利用状況を取得する：
 * Checker14とChecker17の出力を結合した版
 * 
 */
public class Checker18 extends Checker14 {

	@Override
	public void report(final PrintWriter out, final UserList<UserListBean> list) {
		// アドレスを出力してはいけない。拠点ごとに回数を取得しているのに、アドレスを出力すると、回数は実際の値のアドレス数の倍になる
		out.println("ユーザID\t国\tISP/プロジェクトID\t拠点名\tプロジェクト削除\t拠点削除\tユーザ削除\t有効"
				+ "\t初回日時\t最終日時\t接続回数\t失効日時"
				+ "\t接続回数集計\tプロジェクト削除集計\t拠点削除集計\tユーザ削除集計\t終了日時");
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
							, "0"
							, "-1"
							, "-1"
							, "-1"
							, ""
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
								, user.getProjDelFlag()
								, user.getSiteDelFlag()
								, user.getUserDelFlag()
								, site.getEndDate()
								)
								.collect(Collectors.joining("\t")))
							);
				}
			});
	}

	@Override
	public String usage(String name) {
		return String.format("usage: java %s knownlist sslindex [accesslog...]", name);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper(Checker18.class).weld(2, argv);
		System.exit(rc);
	}
}
