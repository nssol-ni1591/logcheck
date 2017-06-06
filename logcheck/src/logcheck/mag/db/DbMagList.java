package logcheck.mag.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.logging.Logger;

import javax.enterprise.inject.Alternative;

import logcheck.annotations.WithElaps;
import logcheck.mag.MagList;
import logcheck.mag.MagListBean;
import logcheck.mag.MagListIsp;
import logcheck.util.DB;
import logcheck.util.net.NetAddr;

@Alternative
public class DbMagList extends LinkedHashMap<String, MagListIsp> implements MagList {

	private static Logger log = Logger.getLogger(DbMagList.class.getName());
	//@Inject private Logger log;

	private static final long serialVersionUID = 1L;

	public static String SQL_ALL_GIP = 
			"select p.prj_id, p.prj_name, s.site_name, g.site_gip"
			+ " from mst_project p, sas_prj_site_info s, sas_site_gip g"
			+ " where p.prj_row_id = s.prj_row_id and s.site_id = g.site_id"
			+ " and g.site_gip != '非固定'"
			+ " and g.site_gip != '追加不要'"
			+ " order by"
			+ "  p.delete_flag"
			+ ", s.delete_flag"
			+ ", g.delete_flag"
			+ ", p.prj_id"
			;
	public static String SQL_ACTIVE_GIP = 
			"select p.prj_id, p.prj_name, s.site_name, g.site_gip"
			+ " from mst_project p, sas_prj_site_info s, sas_site_gip g"
			+ " where p.prj_row_id = s.prj_row_id and s.site_id = g.site_id"
			+ " and p.delete_flag = '0'"
			+ " and s.delete_flag = '0'"
			+ " and g.delete_flag = '0'"
			+ " and g.site_gip != '非固定'"
			+ " and g.site_gip != '追加不要'"
			+ " order by m.prj_id";

	public DbMagList() {
		super(2000);	// for HashMap
	}

	@Override
	public MagListIsp get(NetAddr addr) {
		Optional<MagListIsp> rc = values().stream()
/*
				.filter(isp -> {
//					return isp.getAddress().stream().filter(net -> net.within(addr)).findFirst().isPresent();
					return isp.getAddress().stream().anyMatch(net -> net.within(addr));
				})
*/
				.filter(isp -> isp.getAddress().stream().anyMatch(net -> net.within(addr)))
				.findFirst();
		return rc.isPresent() ? rc.get() : null;
	}

	/*
	 * tsvファイル方式斧互換性のためのメソッド定義
	 * 引数のmagfileは無視される
	 * @see logcheck.mag.MagList#load()
	 */
	@Override
	public MagList load(String magfile) throws Exception {
		return load();
	}

	@Override @WithElaps
	public MagList load() throws Exception {
		String sql = SQL_ALL_GIP;

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
				String projId = rs.getString(1);
				String projName = rs.getString(2);
				String projSite = rs.getString(3);
				String magIp = rs.getString(4);

				if (magIp.split("\\.").length == 4) {
					MagListBean b = new MagListBean(projId, projName, projSite, magIp);

					MagListIsp mp = this.get(b.getProjId());
					if (mp == null) {
						mp = new MagListIsp(b);
						this.put(b.getProjId(), mp);
					}
					NetAddr addr = new NetAddr(b.getMagIp());
					mp.addAddress(addr);
					log.fine(String.format("projId=%s, addr=%s\n", projId, addr));
				}
				else {
//					System.err.printf("WARNING(MAG): prjId=\"%s\", magIp=\"%s\"\n", prjId, magIp);
					log.warning(String.format("(インターネット経由接続先): projId=\"%s\", magIp=\"%s\"", projId, magIp));
				}
			}
		}
		return this;
	}

	public static void main(String[] args) {
		System.out.println("start DbMagList.main ...");
		DbMagList map = new DbMagList();
		try {
			map.load(SQL_ALL_GIP);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String name : map.keySet()) {
			MagListIsp c = map.get(name);
			System.out.println(name + "=" + c.getAddress());
		}
		System.out.println("DbMagList.main ... end");
		System.exit(0);
	}

}
