package logcheck.user.ssl;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
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

	@Inject Logger log;

	private static final long serialVersionUID = 1L;
	private static final String TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

	public static final String SQL_ZUSER =
			"select u.site_id, u.user_id, u.delete_flag, u.end_date"
			+ " from sas_prj_site_user u"
			+ " where u.user_id like 'Z%'"
//	証明書が有効なユーザに関する情報を取得する。その際、過去のPRJは考慮しない
//			+ " and u.delete_flag = '0'"
//	ユーザIDは一意で決定するので、orderを指定する必要はない　<- うそ
			+ " order by　u.delete_flag"
	;

	public SSLUserList() {
		super(4000);
		if (log == null) {
			// logのインスタンスが生成できないため
			log = Logger.getLogger(SSLUserList.class.getName());
		}
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
				input.filter(SSLIndexBean::test)
					.map(SSLIndexBean::parse)
					.filter(b -> b.getUserId().startsWith("Z"))
					.forEach(b -> {
						boolean status = false;
						UserListBean bean = this.get(b.getUserId());
						if (bean == null) {
							try {
								frs.beforeFirst();
								frs.setFilter(new SelectUser(b.getUserId()));

								while (frs.next()) {
									String siteId = frs.getString(1);
									String userDelFlag = frs.getString(3);
									String endDate = "";
									// OracleFilteredRowSet#getTimestampはTimestampをサポートしていないため
									Object o = frs.getObject(4);
									if (o != null) {
										final DateFormat f = new SimpleDateFormat(TIME_FORMAT);
										endDate = f.format(((oracle.sql.TIMESTAMP)o).timestampValue());
									}

									status = true;

									if (bean == null) {
										bean = new UserListBean(b);
										this.put(b.getUserId(), bean);
									}

									SiteListIsp siteBean = sitelist.get(siteId);
									if (siteBean != null) {
										UserListSite site = new UserListSite(siteBean, userDelFlag, endDate);
										bean.addSite(site);
									}
									else {
										log.log(Level.WARNING, "site is null: siteId={0}, bean=[{1}]", new Object[] {siteId, bean});
									}

								}
								if (!status) {
									// sslindexに存在するが、SSLテーブルに存在しない場合：
									// 不正な状態を検知することができるように削除フラグ"-1"でuserlistに追加する
									log.log(Level.WARNING, "user_id not found: sslindex=[{0}]", b);
									bean = new UserListBean(b);
									this.put(b.getUserId(), bean);
								}
							} 
							catch (SQLException e) {
								log.log(Level.SEVERE, "例外", e);
							}
						}
						else {
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
			} 
			catch (SQLException e) {
				return false;
			}
		}
		@Override
		public boolean evaluate(Object value, int column) throws SQLException {
			return false;
		}
		@Override
		public boolean evaluate(Object value, String columnName) throws SQLException {
			return false;
		}
	}

}
