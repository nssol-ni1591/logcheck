package logcheck.site.db;

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
import logcheck.util.DB;

/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
@Alternative
public class DbSiteList extends LinkedHashMap<String, SiteListIsp> implements SiteList {

	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(DbSiteList.class.getName());
/*
	public static String SQL_ALL_SITE = 
			"select s.site_id, s.site_name, s.delete_flag, p.prj_id, p.delete_flag, g.site_gip"
			+ " from mst_project p, sas_prj_site_info s, sas_site_gip g"
			+ " where p.prj_row_id = s.prj_row_id"
			+ " and s.site_id = g.site_id"
			+ " and g.site_gip != '追加不要'"
// 未利用ユーザを確認する時点ですでに無効になっているとエラーになるための処置　->エラーにしなければよいか?
//			+ " and g.site_gip != '非固定'"
//			+ " order by"
//			+ "  p.delete_flag"
//			+ ", s.delete_flag"
	;
*/
	public static String SQL_ALL_SITE = 
			"select s.site_id, s.site_name, s.delete_flag, p.prj_id, p.delete_flag, g.site_gip"
			+ " from mst_project p"
			+ " , masterinfo.sas_prj_site_info s left outer join masterinfo.sas_site_gip g on s.site_id = g.site_id"
			+ " where p.prj_row_id = s.prj_row_id"
	;

	public DbSiteList() {
		super(600);
	}

	@Override @WithElaps
	public SiteList load(String file) throws Exception {

		String sql = SQL_ALL_SITE;

		// Oracle JDBC Driverのロード
		//Class.forName("oracle.jdbc.driver.OracleDriver");

		try (	// Oracleに接続
				//Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@172.31.247.137:1521/sdcdb01", "masterinfo", "masterinfo");
				//Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@172.31.247.137:1521:sdcdb011", "masterinfo", "masterinfo");
				Connection conn = DB.createConnection();
				// ステートメントを作成
				PreparedStatement stmt = conn.prepareStatement(sql);
				)
		{
			ResultSet rs = stmt.executeQuery();
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
					globalIp = "0.0.0.0";	// IPアドレスとしては不正なので一致しない
				}

				SiteListIsp site = this.get(siteId);
				if (site == null) {
					site = new SiteListIspImpl(siteId, siteName, siteDelFlag, projId, projDelFlag);
					this.put(siteId, site);
				}
				site.addAddress(globalIp);
				log.fine(site.toString());
			}
		}
		return this;
	}

	public static void main(String[] argv) {
		System.out.println("start SiteList.main ...");
		DbSiteList map = new DbSiteList();
		try {
			map.load(null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int ix = 0;
		for (String userId : map.keySet()) {
			SiteListIsp b = map.get(userId);
			System.out.println(b);
			ix += 1;
		}
		System.out.println("ix=" + ix);
		System.out.println("UserList.main ... end");
	}

}
