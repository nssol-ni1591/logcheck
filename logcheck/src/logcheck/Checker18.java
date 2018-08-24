package logcheck;

import java.io.PrintWriter;

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
		// 拠点ごとに接続回数を取得しているので、アドレスを出力してはいけない。
		// アドレスを出力すると、接続回数は実際の値のアドレス数の倍になる
		out.println(String.join("\t"
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
				, "接続回数集計"
				, "プロジェクト削除集計"
				, "拠点削除集計"
				, "ユーザ削除集計"
				, "終了日時"));
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
							, "0"
							, "-1"
							, "-1"
							, "-1"
							, ""
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
								, user.getProjDelFlag()
								, user.getSiteDelFlag()
								, user.getUserDelFlag()
								, site.getEndDate()
								))
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
