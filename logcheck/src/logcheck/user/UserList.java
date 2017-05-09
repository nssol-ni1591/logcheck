package logcheck.user;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;

import logcheck.annotations.WithElaps;

/*
 * 有効なVPNクライアント証明書が発行されているユーザの一覧を取得する
 */
public class UserList extends LinkedHashMap<String, UserListBean> {

	private static final long serialVersionUID = 1L;
/*
	public static String SQL_ACTIVE_USER = 
			"select prj.prj_id, site.site_name, site_user.user_id"
			+ " from mst_project prj, sas_prj_site_info site, sas_prj_site_user site_user"
			+ "　where prj.delete_flag = '0'"
			+ " and prj.prj_row_id = site.prj_row_id"
			+ "　and site.site_type_cd = '02'"
			+ " and site.site_id = site_user.site_id"
			+ " and site.delete_flag = '0'"
			+ "　and site_user.delete_flag = '0'";
*/
	public static String SQL_ACTIVE_CERTIFICATION_ZUSER = 
			"select l.user_id , p.prj_id, s.site_name, s.site_type_cd, s.connect_type_cd, p.delete_flag,　s.delete_flag, u.delete_flag"
			+ " from mst_project p, sas_prj_site_info s, sas_prj_site_user u, user_ssl_info l"
			+ " where l.valid_flg = '1'"
//	証明書が有効なユーザに関する情報を取得する。その際、過去のPRJは考慮しない
//			+ " and p.delete_flag = '0'"
//			+ " and s.delete_flag = '0'"
//			+ " and u.delete_flag = '0'"
			+ " and p.prj_row_id = s.prj_row_id"
			+ " and s.site_id = u.site_id"
			+ " and u.user_id = l.user_id"
			+ " and l.user_id like 'Z%'"
			+ " order by l.user_id";

	public UserList() {
		super(1500);
	}

	@WithElaps
	public UserList load(String sql) throws Exception {
		// Oracle JDBC Driverのロード
		Class.forName("oracle.jdbc.driver.OracleDriver");

		try (	// Oracleに接続
				Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@172.31.247.137:1521/sdcdb01", "masterinfo", "masterinfo");
				// ステートメントを作成
				PreparedStatement stmt = conn.prepareStatement(sql);
				)
		{
			ResultSet rs = stmt.executeQuery();
			// 問合せ結果の表示
			while (rs.next()) {
				String userId = rs.getString(1);
				String prjId = rs.getString(2);
				String siteName = rs.getString(3);
				String siteCd = rs.getString(4);
				String connCd = rs.getString(5);
				String prjDelFlag = rs.getString(6);
				String siteDelFlag = rs.getString(7);
				String userDelFlag = rs.getString(8);

				UserListBean b = this.get(userId);
				if (b == null) {
					b = new UserListBean(userId, userDelFlag);
					this.put(userId, b);
				}
				b.addPrjs(new UserListSite(prjId, siteName, siteCd, connCd, prjDelFlag, siteDelFlag));
				//System.out.println(b);
			}
		}
		return this;
	}

	public static void main(String[] args) {
		System.out.println("start UserList.main ...");
		UserList map = new UserList();
		try {
			map.load(SQL_ACTIVE_CERTIFICATION_ZUSER);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String userId : map.keySet()) {
			UserListBean b = map.get(userId);
			System.out.println(b);
		}
		System.out.println("UserList.main ... end");
	}

}
