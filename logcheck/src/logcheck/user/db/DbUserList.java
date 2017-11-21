package logcheck.user.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import javax.enterprise.inject.Alternative;

import logcheck.annotations.WithElaps;
import logcheck.site.SiteList;
import logcheck.site.SiteListIsp;
import logcheck.site.SiteListIspImpl;
import logcheck.user.UserListBean;
import logcheck.user.UserListSite;
import logcheck.user.UserList;
import logcheck.util.DB;

/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
@Alternative
public class DbUserList extends LinkedHashMap<String, UserListBean> implements UserList<UserListBean> {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(DbUserList.class.getName());

	public static final String SQL_ZUSER = 
			"select p.prj_id, p.delete_flag, s.site_id, s.site_name, s.delete_flag, g.site_gip"
					+ " , u.user_id, u.delete_flag, l.valid_flg"
					+ " from mst_project p"
					+ " , sas_prj_site_info s left outer join sas_site_gip  g on s.site_id = g.site_id"
					+ " , sas_prj_site_user u, user_ssl_info l"
					+ " where p.prj_row_id = s.prj_row_id"
					+ " and s.site_id = u.site_id"
					+ " and u.user_id = l.user_id"
					+ " and u.user_id like 'Z%'"
					+ " order by u.delete_flag, s.delete_flag, s.delete_flag"
					;

	private static String getDefaultIp() {
		return "0.0.0.0";
	}

	public DbUserList() {
		super(4000);
	}

	@WithElaps
	public DbUserList load(String file, SiteList sitelist) throws Exception {
		String sql = SQL_ZUSER;
		try (	// Oracleに接続
				Connection conn = DB.createConnection();
				// ステートメントを作成
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery();
				)
		{
			// 問合せ結果の表示
			while (rs.next()) {
				String projId = rs.getString(1);
				String projDelFlag = rs.getString(2);

				String siteId = rs.getString(3);
				String siteName = rs.getString(4);
				String siteDelFlag = rs.getString(5);
				String globalIp = rs.getString(6);

				String userId = rs.getString(7);
				String userDelFlag = rs.getString(8);
				String validFlag = rs.getString(9);

				UserListBean bean = this.get(userId);
				if (bean == null) {
//					bean = new UserListBean(userId, userDelFlag, validFlag);
					bean = new UserListBean(userId, validFlag);
					this.put(userId, bean);
				}

				if (globalIp == null
						|| "非固定".equals(globalIp)
						|| "追加不要".equals(globalIp)) {
					globalIp = getDefaultIp();	// IPアドレスとしては不正なので一致しない for 専用線、ISP経由
				}

//				UserListSummary site = bean.getSite(new NetAddr(globalIp));
				UserListSite site = bean.getSite(siteId);
				if (site == null) {
					SiteListIsp siteBean = new SiteListIspImpl(siteId, siteName, siteDelFlag, projId, projDelFlag);
					site = new UserListSite(siteBean, userDelFlag);
					bean.addSite(site);
				}
				site.addAddress(globalIp);
				log.fine(bean.toString());
			}
		}
		return this;
	}

}
