package logcheck;

import java.io.PrintWriter;
import java.util.stream.Stream;

import logcheck.user.UserList;
import logcheck.user.UserListBean;
import logcheck.user.UserListSite;
import logcheck.util.WeldWrapper;

/*
 * ユーザの利用状況を取得する：
 * Checker14とChecker17の出力を結合した版
 * 
 */
public class Checker18a extends Checker14 {

	/*
	 * 拠点ごとに接続回数を取得しているので、アドレスを出力してはいけない。
	 * アドレスを出力すると、接続回数は実際の値のアドレス数の倍になる
	 */
	@Override
	public void report(final PrintWriter out, final UserList<UserListBean> list) {
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
			.map(OutWrapperC18::new)
			.flatMap(OutWrapperC18::stream)
			.forEach(out::println);
	}

	@Override
	public String usage(String name) {
		return String.format("usage: java %s knownlist sslindex [accesslog...]", name);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper(Checker18a.class).weld(2, argv);
		System.exit(rc);
	}
	
	class OutWrapperC18 extends OutWrapper {
		
		public OutWrapperC18(UserListBean user) {
			super(user);
		}
		public OutWrapperC18(UserListBean user, UserListSite site) {
			super(user, site);
		}

		// 本来ならばstaticにしたいが、innerのため定義できない
		@Override
		public Stream<OutWrapper> stream() {
			if (user.getSites().isEmpty()) {
				return Stream.of(this);
			}
			return user.getSites().stream().map(s -> new OutWrapperC18(user, s));
		}

		@Override
		public String toString() {
			return String.join("\t"
					, getUserId()
					, getCountry()
					, getProjId()
					, getSiteName()
					, getProjDelFlag()
					, getSiteDelFlag()
					, getUserDelFlag()
					, getValidFlag()
					, getFirstDate()
					, getLastDate()
					, getCount()
					, getRevoce()
					, getTotal()
					// 同名のメソッドがあるので、直接クラスを参照する
					, user.getProjDelFlag()
					, user.getSiteDelFlag()
					, user.getUserDelFlag()
					, site == null ? "" : site.getEndDate()
					);
		}
	}

}
