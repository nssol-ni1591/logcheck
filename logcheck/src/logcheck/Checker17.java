package logcheck;

import java.io.PrintWriter;

import logcheck.user.UserList;
import logcheck.user.UserListBean;
import logcheck.util.weld.WeldWrapper;

/*
 * ユーザの利用状況を取得する：
 * 出力形式をユーザ単位の出力に変更する（checker14は拠点単位）
 * 各削除フラグは、所属する全ての拠点のうち一つでも未削除("0")のときのみ削除されていないと判定する。（UserListBeanに実装）
 * このため、Check14では面倒な、各削除フラムの状態をフィルタすることが可能。だが、所属するプロジェクト、拠点の情報は出力されない
 */
public class Checker17 extends Checker14 {

	@Override
	public void report(final PrintWriter out, final UserList<UserListBean> list) {
		// アドレスを出力してはいけない。拠点ごとに回数を取得しているのに、アドレスを出力すると、回数は実際の値のアドレス数の倍になる
		out.println("ユーザID\tプロジェクト削除\t拠点削除\tユーザ削除\t有効\t初回日時\t最終日時\t失効日時\tユーザ回数");
		userlist.values().stream()
			.forEach(user -> {
				if (user.getSites().isEmpty()) {
					out.println(new StringBuilder(user.getUserId())
							.append("\t").append("-1")
							.append("\t").append("-1")
							.append("\t").append("-1")
							.append("\t").append(user.getValidFlag())
							.append("\t").append("")
							.append("\t").append("")
							.append("\t").append(user.getRevoce())
							.append("\t").append("0")
							);
				}
				else {
					out.println(new StringBuilder(user.getUserId())
							.append("\t").append(user.getProjDelFlag())
							.append("\t").append(user.getSiteDelFlag())
							.append("\t").append(user.getUserDelFlag())
							.append("\t").append(user.getValidFlag())
							.append("\t").append(user.getFirstDate())
							.append("\t").append(user.getLastDate())
							.append("\t").append(user.getRevoce())
							.append("\t").append(user.getTotal())
							);
				}
			});
	}

	@Override
	public String usage(String name) {
		return String.format("usage: java %s knownlist sslindex [accesslog...]", name);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper<Checker17>(Checker17.class).weld(2, argv);
		System.exit(rc);
	}
}
