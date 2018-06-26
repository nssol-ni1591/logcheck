package logcheck.proj.db;

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
import logcheck.proj.ProjList;
import logcheck.proj.ProjListBean;
import logcheck.util.DB;

/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
@Alternative
public class DbProjList extends LinkedHashMap<String, ProjListBean> implements ProjList<ProjListBean> {

	@Inject private transient Logger log;

	private static final long serialVersionUID = 1L;

	public static final String SQL_ALL_PROJ = 
			"select p.prj_id, p.delete_flag"
			+ " from mst_project p"
			+ " order by p.delete_flag"
			;

	public DbProjList() {
		super(600);
	}

	// for envoronment not using weld-se
	public void init() {
		if (log == null) {
			// not weld-seの場合、logのインスタンスが生成できないため
			log = Logger.getLogger(this.getClass().getName());
		}
	}

	@Override @WithElaps
	public ProjList<ProjListBean> load() throws IOException, ClassNotFoundException, SQLException {
		// @Overrideのため、使用しない引数のfileを定義する
		String sql = SQL_ALL_PROJ;

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

				put(projId, new ProjListBean(projId, projDelFlag));
				log.log(Level.FINE, "DbProjList={0}", projId);
			}
		}
		catch (SQLException ex) {
			log.log(Level.SEVERE, "catch SQLException", ex);
		}
		return this;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

}
