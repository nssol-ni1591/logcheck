package logcheck.user;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import logcheck.annotations.WithElaps;
import logcheck.isp.IspList;
import logcheck.util.DB;
import logcheck.util.net.NetAddr;

/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
public class UserList<E extends IspList> extends LinkedHashMap<String, UserListBean<E>> {

	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(UserList.class.getName());
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
			"select l.user_id , p.prj_id, s.site_name, g.site_gip, p.delete_flag,　s.delete_flag, u.delete_flag, l.valid_flg"
//			"select l.user_id , p.prj_id, s.site_name, g.site_gip, s.site_type_cd, s.connect_type_cd, p.delete_flag,　s.delete_flag, u.delete_flag, l.valid_flg"
//			"select l.user_id , p.prj_id, s.site_name"
			+ " from mst_project p, sas_prj_site_info s, sas_prj_site_user u, user_ssl_info l, sas_site_gip g"
			+ " where l.user_id like 'Z%'"
//	証明書が有効なユーザに関する情報を取得する。その際、過去のPRJは考慮しない
//			+ " and p.delete_flag = '0'"
//			+ " and s.delete_flag = '0'"
//			+ " and u.delete_flag = '0'"
			+ " and p.prj_row_id = s.prj_row_id"
			+ " and s.site_id = u.site_id"
			+ " and u.user_id = l.user_id"
			+ " and s.site_id = g.site_id"
// 未利用ユーザを確認する時点ですでに無効になっているとエラーになるための処置　->エラーにしなければよいか?
//			+ " and l.valid_flg = '1'"
			+ " and g.site_gip != '非固定'"
			+ " and g.site_gip != '追加不要'"
			+ " order by"
			+ "  p.delete_flag"
			+ ", s.delete_flag"
			+ ", u.delete_flag"
			+ ", l.user_id"
	;

	public UserList() {
		super(4000);
	}

	public UserList<E> load(Class<E> clazz) throws Exception {
		return load(SQL_ACTIVE_CERTIFICATION_ZUSER, clazz);
	}

	@WithElaps
	public UserList<E> load(String sql, Class<E> clazz) throws Exception {
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
				String userId = rs.getString(1);
				String projId = rs.getString(2);
				String siteName = rs.getString(3);
				String siteIp = rs.getString(4);
				String projDelFlag = rs.getString(5);
				String siteDelFlag = rs.getString(6);
				String userDelFlag = rs.getString(7);
				String validFlag = rs.getString(8);

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
		return this;
	}

	public static void main(String[] args) {
		System.out.println("start UserList.main ...");
		UserList<UserListSummary> map = new UserList<>();
		try {
			map.load(UserListSummary.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int ix = 0;
		for (String userId : map.keySet()) {
			UserListBean<UserListSummary> b = map.get(userId);
			String userDel = "0".equals(b.getUserDelFlag()) ? " " : "*";
//			String siteDel = b.isDelFlag() ? "*" : " ";
//			String siteDel = b.getSites().stream().filter(site -> site.isDelFlag()).findFirst().isPresent() ? "*" : " ";
			String siteDel = b.getSites().stream().allMatch(site -> site.isDelFlag()) ? "*" : " ";
			System.out.println(userDel + siteDel + " " + b);
			ix += 1;
		}
		System.out.println("ix=" + ix);
		System.out.println("UserList.main ... end");
	}

}
