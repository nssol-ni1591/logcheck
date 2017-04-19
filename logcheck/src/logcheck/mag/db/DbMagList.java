package logcheck.mag.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Optional;

import logcheck.mag.MagList;
import logcheck.mag.MagListBean;
import logcheck.mag.MagListIsp;
import logcheck.util.NetAddr;

public class DbMagList extends HashMap<String, MagListIsp> implements MagList {

	private static final long serialVersionUID = 1L;

	public static String SQL_ALL_GIP = "select m.prj_id, m.prj_name, p.site_name, g.site_gip"
			+ " from masterinfo.mst_project m, masterinfo.sas_prj_site_info p, masterinfo.sas_site_gip g"
			+ " where m.prj_row_id = p.prj_row_id and p.site_id = g.site_id"
//			+ " and m.delete_flag = '0'"
//			+ " and p.delete_flag = '0'"
//			+ " and g.delete_flag = '0'"
			+ " and g.site_gip != '非固定'"
//			+ " and g.site_gip != '追加不要'"
			+ " order by m.prj_id";
	public static String SQL_ACTIVE_GIP = "select m.prj_id, m.prj_name, p.site_name, g.site_gip"
			+ " from masterinfo.mst_project m, masterinfo.sas_prj_site_info p, masterinfo.sas_site_gip g"
			+ " where m.prj_row_id = p.prj_row_id and p.site_id = g.site_id"
			+ " and m.delete_flag = '0'"
			+ " and p.delete_flag = '0'"
			+ " and g.delete_flag = '0'"
			+ " and g.site_gip != '非固定'"
//			+ " and g.site_gip != '追加不要'"
			+ " order by m.prj_id";

	@Override
	public MagListIsp get(NetAddr addr) {
		Optional<MagListIsp> rc = values().stream().filter(isp -> {
			return isp.getAddress().stream().filter(net -> net.within(addr)).findFirst().isPresent();
		}).findFirst();
		return rc.isPresent() ? rc.get() : null;
	}

	@Override
	public MagList load(String file) throws Exception {
		// Oracle JDBC Driverのロード
		Class.forName("oracle.jdbc.driver.OracleDriver");

		try (	// Oracleに接続
				Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@172.31.247.139:1521:sdcdb011", "masterinfo", "masterinfo");
				// ステートメントを作成
				PreparedStatement stmt = conn.prepareStatement(SQL_ALL_GIP);
				)
		{
			ResultSet rs = stmt.executeQuery();
			// 問合せ結果の表示
			while (rs.next()) {
				String prjId = rs.getString(1);
				String prjName = rs.getString(2);
				String prjSite = rs.getString(3);
				String magIp = rs.getString(4);

				if (magIp.split("\\.").length == 4) {
					MagListBean b = new MagListBean(prjId, prjName, prjSite, magIp);

					MagListIsp mp = this.get(b.getPrjId());
					if (mp == null) {
						mp = new MagListIsp(b.getPrjId());
						this.put(b.getPrjId(), mp);
					}
					NetAddr addr = new NetAddr(b.getMagIp());
					mp.addAddress(addr);
					//System.out.printf("prjId=%s, addr=%s\n", prjId, addr);
				}
				else {
					System.err.printf("WARNING: prjId=%s, magIp=%s\n", prjId, magIp);
				}
			}
		}
		return this;
	}

	public static void main(String[] args) {
		System.out.println("start DbMagList.main ...");
		DbMagList map = new DbMagList();
		try {
			map.load(null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String name : map.keySet()) {
			MagListIsp c = map.get(name);
			System.out.println(name + "=" + c.getAddress());
		}
		System.out.println("DbMagList.main ... end");
	}

}
