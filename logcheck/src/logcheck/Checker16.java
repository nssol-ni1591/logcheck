package logcheck;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import logcheck.user.UserList;

/*
 * 未利用ユーザ検索：
 * Checker14をスーパークラスとして、出力のみを絞り込む
 * 
 */
public class Checker16 extends Checker14 {

	/*
	 *  from 上野さん  (2017/05/26 (金) 15:03)
	 * 上野です。お疲れ様です。
	 * 可能であれば、後で並び替えが自由にできるようにテキストであれば、CSV等Excelで開けて、並び替えができるとよいです。
	 * 万一、既に考慮に入っておりましたら申し訳ございません。
	 * また、さきほどお話した際に見せて頂いたように、利用申請側の下記項目も追加であると今後、棚卸しやすいと思います。
	 * ・プロジェクトの有効/無効フラグ
	 * ・拠点の有効/無効フラグ
	 * ・ユーザの有効/無効フラグ
	 * 
	 * とのことなので、効率は悪いが、Excelで並び替え想定なのでソートは必要なし。各削除フラムの情報も出力する。
	 * @see logcheck.Checker14#report(logcheck.user.UserList)
	 */
	public void report(UserList map) {
		System.out.println("ユーザID\tプロジェクトID\t拠点名\tプロジェクト削除\t拠点削除\tユーザ削除");
		map.values().stream()
			// ツール実行時点で証明書が無効ならば、利用状況を確認する必要がないので対象外にする
			.filter(user -> user.sumCount() == 0 && "1".equals(user.getValidFlag()))
			.forEach(user -> {
				user.getSites().forEach(site -> {
					System.out.println(
							new StringBuilder(user.getUserId())
							.append("\t")
							.append(site.getProjId())
							.append("\t")
							.append(site.getSiteName())
							.append("\t")
							.append(site.getProjDelFlag())
							.append("\t")
							.append(site.getSiteDelFlag())
							.append("\t")
							.append(user.getUserDelFlag())
							);
			});
		});
	}

	public static void main(String... argv) {
		if (argv.length < 1) {
			System.err.println("usage: java logcheck.Checker16 knownlist [accesslog...]");
			System.exit(1);
		}

		int rc = 0;
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker16 application = container.instance().select(Checker16.class).get();
			application.init(argv[0]).start(argv, 1);
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
			rc = 1;
		}
		System.exit(rc);
	}
}