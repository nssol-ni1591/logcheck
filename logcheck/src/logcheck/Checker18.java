package logcheck;

import java.io.PrintWriter;

import logcheck.user.UserList;
import logcheck.user.UserListBean;
import logcheck.util.weld.WeldWrapper;

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
				+ "\t接続回数集計\tプロジェクト削除集計\t拠点削除集計\tユーザ削除集計");
		userlist.values().stream()
			.forEach(user -> {
				if (user.getSites().isEmpty()) {
					out.println(new StringBuilder(user.getUserId())
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
							.append("\t").append("-1")
							.append("\t").append("-1")
							.append("\t").append("-1")
							);
				}
				else {
					int total = user.getTotal();
					String projDelFlag = user.getProjDelFlag();
					String siteDelFlag = user.getSiteDelFlag();
					String userDelFlag = user.getUserDelFlag();
					user.getSites().forEach(site -> 
						out.println(new StringBuilder(user.getUserId())
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
								.append("\t").append(total)
								.append("\t").append(projDelFlag)
								.append("\t").append(siteDelFlag)
								.append("\t").append(userDelFlag)
								)
					);
				}
			});
	}

	@Override
	public String usage(String name) {
		return String.format("usage: java %s knownlist sslindex [accesslog...]", name);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper<Checker18>(Checker18.class).weld(2, argv);
		System.exit(rc);
	}
}
