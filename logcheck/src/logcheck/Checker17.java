package logcheck;

import java.io.PrintWriter;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import logcheck.user.UserList;
import logcheck.user.UserListBean;

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

	public static void main(String... argv) {
		if (argv.length < 2) {
			System.err.println("usage: java logcheck.Checker17 knownlist sslindex [accesslog...]");
			System.exit(1);
		}

		int rc = 0;
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker17 application = container.instance().select(Checker17.class).get();
//			Checker17 application = container.instance().select(Checker17.class, new AnnotationLiteral<UseChecker14>(){
//				private static final long serialVersionUID = 1L;
//			}).get();
			application.init(argv[0], argv[1]).start(argv, 2);
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
			rc = 1;
		}
		System.exit(rc);
	}
}
