package logcheck.user.ssl;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import javax.enterprise.inject.Alternative;
import javax.sql.RowSet;
import javax.sql.rowset.FilteredRowSet;
import javax.sql.rowset.Predicate;

import logcheck.annotations.WithElaps;
import logcheck.site.SiteList;
import logcheck.site.SiteListIsp;
import logcheck.site.db.DbSiteList;
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

	public static String SQL_ZUSER =
			"select u.site_id, u.user_id, u.delete_flag"
			+ " from sas_prj_site_user u"
			+ " where u.user_id like 'Z%'"
//	証明書が有効なユーザに関する情報を取得する。その際、過去のPRJは考慮しない
//			+ " and u.delete_flag = '0'"
//			+ " order by"
//			+ ", u.user_id"
	;
//	@Inject private SiteList sitelist;

	public SSLUserList() {
		super(4000);
	}
/*
	@PostConstruct
	private void init() {
		// かっこ悪いけど ... いい方法があれば教えて
		if (sitelist == null) {
			sitelist = new DbSiteList();
		}
		try {
			sitelist.load(null);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
*/
	private FilteredRowSet getRowSet(String sql) throws Exception {
		try ( // Oracleに接続
				Connection conn = DB.createConnection();
				// ステートメントを作成
				PreparedStatement stmt = conn.prepareStatement(sql);
				)
		{
			ResultSet rs = stmt.executeQuery();

			FilteredRowSet frs = new OracleFilteredRowSet();
			frs.populate(rs);
			return frs;
		}
	}
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
//			log.warning("(SSL not Z): s=\"" + s.trim() + "\"");
			rc = false;
		}
		return rc;
	}

	public SSLUserList load() throws Exception {
		throw new IllegalArgumentException("must file");
	}

	@WithElaps
	public SSLUserList load(String file, SiteList sitelist) throws Exception {
		FilteredRowSet rs = getRowSet(SQL_ZUSER);

		Files.lines(Paths.get(file), Charset.forName("utf-8"))
			.filter(s -> test(s))
			.map(s -> parse(s))
			.forEach(b -> {
				UserListBean bean = this.get(b.getUserId());
				if (bean == null) {
//					log.info(b.toString());
					try {
//						rs.first();
						rs.beforeFirst();
						rs.setFilter(new SelectUser(b.getUserId()));

						while (rs.next()) {
							String siteId = rs.getString(1);
//							String userId = rs.getString(2);
							String userDelFlag = rs.getString(3);

							if (bean == null) {
								bean = new UserListBean(b, userDelFlag);
								this.put(b.getUserId(), bean);
							}

							SiteListIsp siteBean = sitelist.get(siteId);
							if (siteBean != null) {
								UserListSite site = new UserListSite(siteBean);
								bean.addSite(site);
//								log.info(site.toString());
							}
							else {
								log.warning("site is null: siteId=" + siteId + ", bean=[" + bean + "]");
							}
//							log.info(bean.toString());
						}
					}
					catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				else {
//					log.warning("(SSLインデックス): site=" + site);
					bean.update(b);
				}
			});
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

	public static void main(String[] argv) {
		System.out.println("start SSLUserList.main ...");
		SSLUserList map = new SSLUserList();
		try {
//			map.init();
			map.load(argv[0], new DbSiteList().load(null));
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int ix = 0;
		int iy = 0;
		for (String userId : map.keySet()) {
			UserListBean b = map.get(userId);
			for (UserListSite sum : b.getSites()) {
				System.out.println("userId=" + userId + " (" + b.getValidFlag() + "), sum=[" + sum + "]");
				ix += 1;
			}
			iy += 1;
		}
		System.out.println("user.count=" + iy + ", gip.count=" + ix);
		System.out.println("SSLUserList.main ... end");
	}

}
