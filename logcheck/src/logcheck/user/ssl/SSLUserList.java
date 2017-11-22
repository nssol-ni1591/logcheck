package logcheck.user.ssl;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.enterprise.inject.Alternative;
import javax.sql.RowSet;
import javax.sql.rowset.FilteredRowSet;
import javax.sql.rowset.Predicate;

import logcheck.annotations.WithElaps;
import logcheck.site.SiteList;
import logcheck.site.SiteListIsp;
import logcheck.user.UserListSite;
import logcheck.user.UserList;
import logcheck.user.UserListBean;
import logcheck.user.sslindex.SSLIndexBean;
import logcheck.util.DB;

import oracle.jdbc.rowset.OracleFilteredRowSet;

/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
@Alternative
public class SSLUserList extends LinkedHashMap<String, UserListBean> implements UserList<UserListBean> {

	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(SSLUserList.class.getName());

	public static final String SQL_ZUSER =
			"select u.site_id, u.user_id, u.delete_flag"
			+ " from sas_prj_site_user u"
			+ " where u.user_id like 'Z%'"
//	証明書が有効なユーザに関する情報を取得する。その際、過去のPRJは考慮しない
//			+ " and u.delete_flag = '0'"
//	ユーザIDは一意で決定するので、orderを指定する必要はない　<- うそ
			+ " order by　u.delete_flag"
	;

	public SSLUserList() {
		super(4000);
	}
/*
	private FilteredRowSet getRowSet(String sql) throws Exception {
		try ( // Oracleに接続
				Connection conn = DB.createConnection();
				// ステートメントを作成
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery();
				)
		{
			// このメソッド内ではFilteredRowSet frsをclose()しないで、呼出元で使い終わったのちにclose()すること
			FilteredRowSet frs = new OracleFilteredRowSet();
			frs.populate(rs);
			return frs;
		}
	}
*/
	private SSLIndexBean parse(String s) {
		String[] array = s.split("\t");
		String flag = array[0];
		String expire = array[1];
		String revoce = array[2];
		String serial = array[3];
		String filename = array[4];

		int pos = array[5].indexOf("/CN=");
		String userId = array[5].substring(pos + 4, array[5].length());

		return new SSLIndexBean(flag, expire, revoce, serial, filename, userId);
	}
	private boolean test(String s) {
		boolean rc = false;
		String[] array = s.split("\t");
		if (array.length == 6) {
			int pos = s.indexOf("/CN=");
			if (pos >= 0) {
				rc = true;
			}
		}

		if (!rc) {
			log.warning("(SSLインデックス): s=\"" + s.trim() + "\"");
		}
		// 対象をZユーザに絞る
		if (!s.contains("/CN=Z")) {
			rc = false;
		}
		return rc;
	}

	@WithElaps
	public SSLUserList load(String file, SiteList sitelist) throws Exception {

		try ( // Oracleに接続
				Connection conn = DB.createConnection();
				// ステートメントを作成
				PreparedStatement stmt = conn.prepareStatement(SQL_ZUSER);
				ResultSet rs = stmt.executeQuery();
				FilteredRowSet frs = new OracleFilteredRowSet())
		{
			// オフラインRowSetを取得
			frs.populate(rs);

			rs.close();
			stmt.close();
			conn.close();

			try (Stream<String> input = Files.lines(Paths.get(file), Charset.forName("utf-8")))
			{
				input.filter(s -> test(s)).map(s -> parse(s)).forEach(b -> {
					boolean status = false;
					UserListBean bean = this.get(b.getUserId());
					if (bean == null) {
						try {
							frs.beforeFirst();
							frs.setFilter(new SelectUser(b.getUserId()));

							while (frs.next()) {
								String siteId = frs.getString(1);
								// String userId = frs.getString(2);
								String userDelFlag = frs.getString(3);

								// if (status) {
								// log.warning("site already exist: siteId=" + siteId + ", bean=[" + bean +
								// "]");
								// }
								status = true;

								if (bean == null) {
									// bean = new UserListBean(b, userDelFlag);
									bean = new UserListBean(b);
									this.put(b.getUserId(), bean);
								}

								SiteListIsp siteBean = sitelist.get(siteId);
								if (siteBean != null) {
									UserListSite site = new UserListSite(siteBean, userDelFlag);
									bean.addSite(site);
								} else {
									log.warning("site is null: siteId=" + siteId + ", bean=[" + bean + "]");
								}

							}
							if (!status) {
								// sslindexに存在するが、SSLテーブルに存在しない場合：
								// 不正な状態を検知することができるように削除フラグ"-1"でuserlistに追加する
								log.warning("user_id not found: sslindex=[" + b + "]");
								// bean = new UserListBean(b, "-1");
								bean = new UserListBean(b);
								this.put(b.getUserId(), bean);
							}
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							log.log(Level.SEVERE, "例外", e);
						}

					} else {
						// 以前の証明書が失効している場合
						bean.update(b);
					}
				});
			}
		}
		return this;
	}

	class SelectUser implements Predicate {

		private final String userId;

		public SelectUser(String userId) {
			this.userId = userId;
		}

		@Override
		public boolean evaluate(RowSet rs) {
			try {
				return userId.equals(rs.getString(2));
			} catch (SQLException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
			}
			return false;
		}
		@Override
		public boolean evaluate(Object value, int column) throws SQLException {
			// TODO Auto-generated method stub
			return false;
		}
		@Override
		public boolean evaluate(Object value, String columnName) throws SQLException {
			// TODO Auto-generated method stub
			return false;
		}
	}

}
