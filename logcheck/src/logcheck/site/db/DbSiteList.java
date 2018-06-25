package logcheck.site.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import logcheck.annotations.WithElaps;
import logcheck.site.SiteList;
import logcheck.site.SiteListIsp;
import logcheck.site.SiteListIspImpl;
import logcheck.util.Constants;
import logcheck.util.DB;
import logcheck.util.net.NetAddr;

/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
@Alternative
public class DbSiteList extends LinkedHashMap<String, SiteListIsp> implements SiteList {

	@Inject private transient Logger log;

	private static final long serialVersionUID = 1L;

	public static final String SQL_ALL_SITE = 
			"select s.site_id, s.site_name, s.delete_flag, p.prj_id, p.delete_flag, g.site_gip"
			+ " from mst_project p"
			+ " , sas_prj_site_info s left outer join sas_site_gip g on s.site_id = g.site_id"
			+ " where p.prj_row_id = s.prj_row_id"
			+ " order by p.delete_flag, s.delete_flag"
			;

	public DbSiteList() {
		super(600);
	}

	public void init() {
		if (log == null) {
			// JUnitの場合、logのインスタンスが生成できないため
			log = Logger.getLogger(this.getClass().getName());
		}
	}

	@Override 
	@WithElaps
	public SiteList load(String file) throws IOException, ClassNotFoundException, SQLException {
		// @Overrideのため、使用しない引数のfileを定義する
		String sql = SQL_ALL_SITE;

		try (	// Oracleに接続
				Connection conn = DB.createConnection();
				// ステートメントを作成
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery();
				)
		{
			// 問合せ結果の表示
			while (rs.next()) {
				String siteId = rs.getString(1);
				String siteName = rs.getString(2);
				String siteDelFlag = rs.getString(3);

				String projId = rs.getString(4);
				String projDelFlag = rs.getString(5);

				String globalIp = rs.getString(6);

				SiteListIsp site = this.get(siteId);
				if (site == null) {
					site = new SiteListIspImpl(siteId, siteName, siteDelFlag, projId, projDelFlag);
					this.put(siteId, site);
				}

				NetAddr addr;
				try {
					addr = new NetAddr(globalIp);
					log.log(Level.FINEST, "DbSiteList={0}", site.toString());
				}
				catch (Exception e) {
					addr = new NetAddr(Constants.GLOBAL_IP);
					log.log(Level.INFO, "DbSiteList={0}, globalIp={1}、ex={2}",
							new Object[] { site.toString(), globalIp, e.getMessage() });
				}
				site.addAddress(addr);
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
