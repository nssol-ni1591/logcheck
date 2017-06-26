package logcheck.user.openssl;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import logcheck.isp.IspList;
import logcheck.user.UserList;
import logcheck.user.UserListBean;
import logcheck.user.db.DbUserList;
import logcheck.util.DB;
import logcheck.util.net.NetAddr;

public class OpensslUserList<E extends IspList> extends LinkedHashMap<String, UserListBean<E>> implements UserList<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(DbUserList.class.getName());

	public static String SQL_ACTIVE_CERTIFICATION_ZUSER = 
			"select p.prj_id, s.site_name, g.site_gip, p.delete_flag,　s.delete_flag, u.delete_flag"
			+ " from mst_project p, sas_prj_site_info s, sas_prj_site_user u, sas_site_gip g"
// 一覧で取得するか
//			+ " where u.user_id like 'Z%'"
			+ " where u.user_id = %1"
//	証明書が有効なユーザに関する情報を取得する。その際、過去のPRJは考慮しない
//			+ " and p.delete_flag = '0'"
//			+ " and s.delete_flag = '0'"
//			+ " and u.delete_flag = '0'"
			+ " and p.prj_row_id = s.prj_row_id"
			+ " and s.site_id = u.site_id"
			+ " and s.site_id = g.site_id"
// 未利用ユーザを確認する時点ですでに無効になっているとエラーになるための処置　->エラーにしなければよいか?
//			+ " and g.site_gip != '非固定'"
			+ " and g.site_gip != '追加不要'"
			+ " order by"
			+ "  p.delete_flag"
			+ ", s.delete_flag"
			+ ", u.delete_flag"
			+ ", u.user_id"
	;

	private Map<String, SslIndexBean> index = new LinkedHashMap<>();

	public OpensslUserList() {
		// 20170626現在、上記条件のZ番号ユーザのレコード数は約25000程度。復讐プロジェクト、拠点は別レコードになるためか？
		super(4000);
	}

	@Override
	public UserList<E> load(Class<E> clazz) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserList<E> load(String file, Class<E> clazz) throws Exception {
		
		return null;
	}

	private UserListBean<E> select(String userId, String validFlag) {
		String sql = SQL_ACTIVE_CERTIFICATION_ZUSER;

		try (	// Oracleに接続
				Connection conn = DB.createConnection();
				// ステートメントを作成
				PreparedStatement stmt = conn.prepareStatement(sql);
				)
		{
			ResultSet rs = stmt.executeQuery();
			// 問合せ結果の表示
			while (rs.next()) {
				String projId = rs.getString(1);
				String siteName = rs.getString(2);
				String siteIp = rs.getString(3);
				String projDelFlag = rs.getString(4);
				String siteDelFlag = rs.getString(5);
				String userDelFlag = rs.getString(6);

				if ("非固定".equals(siteIp)) {
					siteIp = "0.0.0.0";
				}

				UserListBean<E> b = this.get(userId);
				if (b == null) {
					b = new UserListBean<>(userId, userDelFlag, validFlag);
					this.put(userId, b);
				}
				NetAddr siteAddr = new NetAddr(siteIp);
				E site = b.getSite(projId, siteName);
				if (site == null) {
//					site = new UserListSummary(projId, siteName, siteAddr, projDelFlag, siteDelFlag);
					Class<?>[] types = { String.class, String.class, NetAddr.class, String.class, String.class };
					Object[] args = { projId, siteName, siteAddr, projDelFlag, siteDelFlag };
					Constructor<E> c = clazz.getConstructor(types);
					site = c.newInstance(args);

					b.addSite(site);
				}
				else {
					site.addAddress(siteAddr);
				}
				log.fine(b.toString());		// デバックmainでは使用不可
			}
		}
		return null;
	}
	@Override
	public UserListBean<E> get(Object userId) /*throws Exception*/ {
		UserListBean<E> b = super.get(userId);
		if (b == null) {
			
		}
		return b;
	}

}
