package logcheck.user.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import logcheck.annotations.WithElaps;
import logcheck.site.SiteList;
import logcheck.site.SiteListIsp;
import logcheck.site.SiteListIspImpl;
import logcheck.user.UserListBean;
import logcheck.user.UserListSite;
import logcheck.user.UserList;
import logcheck.util.Constants;
import logcheck.util.DB;
import logcheck.util.NetAddr;

/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
@Alternative
public class DbUserList extends LinkedHashMap<String, UserListBean> implements UserList<UserListBean> {

	@Inject private Logger log;

	private static final long serialVersionUID = 1L;
	private static final String TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

	public static final String SQL_ZUSER = 
			"select p.prj_id, p.delete_flag, s.site_id, s.site_name, s.delete_flag, g.site_gip"
					+ " , u.user_id, u.delete_flag, l.valid_flg, u.end_date"
					+ " from mst_project p"
					+ " , sas_prj_site_info s left outer join sas_site_gip  g on s.site_id = g.site_id"
					+ " , sas_prj_site_user u, user_ssl_info l"
					+ " where p.prj_row_id = s.prj_row_id"
					+ " and s.site_id = u.site_id"
					+ " and u.user_id = l.user_id"
					+ " and u.user_id like 'Z%'"
					+ " order by u.delete_flag, s.delete_flag, s.delete_flag"
					;

	public DbUserList() {
		super(4000);
	}

	// for envoronment not using weld-se
	public void init() {
		if (log == null) {
			// JUnitの場合、logのインスタンスが生成できないため
			log = Logger.getLogger(this.getClass().getName());
		}
	}

	@WithElaps
	public DbUserList load(String file, SiteList sitelist)
			throws IOException, ClassNotFoundException, SQLException {
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
				Timestamp d = rs.getTimestamp(10);
				String endDate = "";
				if (d != null) {
					final DateFormat f = new SimpleDateFormat(TIME_FORMAT);
					endDate = f.format(d);
				}

				UserListBean bean = this.get(userId);
				if (bean == null) {
					bean = new UserListBean(userId, validFlag);
					this.put(userId, bean);
				}

				UserListSite site = bean.getSite(siteId);
				if (site == null) {
					SiteListIsp siteBean = new SiteListIspImpl(siteId, siteName, siteDelFlag, projId, projDelFlag);
					site = new UserListSite(siteBean, userDelFlag, endDate);
					bean.addSite(site);
				}

				NetAddr addr;
				try {
					addr = new NetAddr(globalIp);
				}
				catch (Exception e) {
					addr = new NetAddr(Constants.GLOBAL_IP);
				}
				site.addAddress(addr);

				log.log(Level.FINE, "DbUserList: UserListBean={0}", bean);
			}
		}
		return this;
	}

	// equals()を実装するとhashCode()の実装も要求され、それはBugにランク付けられるのでequals()の実装をやめたい
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}
}
