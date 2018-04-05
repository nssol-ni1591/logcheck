package logcheck.site.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import logcheck.annotations.WithElaps;
import logcheck.site.SiteList;
import logcheck.site.SiteListIsp;
import logcheck.site.SiteListIspImpl;
import logcheck.util.DB;

/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
@Alternative
public class DbSiteList extends LinkedHashMap<String, SiteListIsp> implements SiteList {

	@Inject private Logger log;

	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_IP = "0.0.0.0";

	public static final String SQL_ALL_SITE = 
			"select s.site_id, s.site_name, s.delete_flag, p.prj_id, p.delete_flag, g.site_gip"
			+ " from mst_project p"
			+ " , sas_prj_site_info s left outer join sas_site_gip g on s.site_id = g.site_id"
			+ " where p.prj_row_id = s.prj_row_id"
			+ " order by p.delete_flag, s.delete_flag"
			;

	public DbSiteList() {
		super(600);
		if (log == null) {
			// logのインスタンスが生成できないため
			log = Logger.getLogger(DbSiteList.class.getName());
		}
	}

	@Override @WithElaps
	public SiteList load(String file) throws Exception {
		// @Overrideのため、使用しない引数のfileを定義する
		String sql = SQL_ALL_SITE;

		// Oracle JDBC Driverのロード
		// なぜコメントアウトで動作する？：Class.forName("oracle.jdbc.driver.OracleDriver");
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

				if (globalIp == null
						|| "非固定".equals(globalIp)
						|| "追加不要".equals(globalIp)) {
					globalIp = DEFAULT_IP;	// IPアドレスとしては不正なので一致しない
				}

				SiteListIsp site = this.get(siteId);
				if (site == null) {
					site = new SiteListIspImpl(siteId, siteName, siteDelFlag, projId, projDelFlag);
					this.put(siteId, site);
				}
				site.addAddress(globalIp);
				log.log(Level.FINE, "DbSiteList={0}", site.toString());
			}
		}
		return this;
	}
	/*
	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	*/
}
