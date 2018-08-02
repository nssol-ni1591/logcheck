package logcheck;

import java.io.PrintWriter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import logcheck.user.UserListBean;
import logcheck.user.UserListSite;
import logcheck.util.WeldWrapper;
import logcheck.user.UserList;

/*
 * 未利用ユーザ検索：
 * Checker14をスーパークラスとして出力項目を変更する
 * 出力項目は上野さんのリクエストで各テーブルの削除フラグを出力する
 */
public class Checker16 extends Checker14 {

	/*
	 * from 上野さん  (2017/05/26 (金) 15:03)
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
	@Override
	public void report(final PrintWriter out, final UserList<UserListBean> map) {
		out.println("ユーザID\tISP/プロジェクトID\t拠点名\tプロジェクト削除\t拠点削除\tユーザ削除");
		map.values().stream()
			// ツール実行時点で証明書が無効ならば、利用状況を確認する必要がないので対象外にする
			.filter(user -> 
				user.getSites().stream().mapToInt(UserListSite::getCount).sum() == 0
				&& "1".equals(user.getValidFlag())
			)
			.forEach(user -> 
				user.getSites().forEach(site -> 
					out.println(Stream.of(user.getUserId()
							, site.getProjId()
							, site.getSiteName()
							, site.getProjDelFlag()
							, site.getSiteDelFlag()
							, site.getUserDelFlag()
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
		int rc = new WeldWrapper(Checker16.class).weld(2, argv);
		System.exit(rc);
	}
}
