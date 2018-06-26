package logcheck.user.ssl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import logcheck.annotations.WithElaps;
import logcheck.site.SiteList;
import logcheck.site.SiteListIsp;
import logcheck.user.UserListSite;
import logcheck.user.UserList;
import logcheck.user.UserListBean;
import logcheck.user.sslindex.SSLIndexBean;
import logcheck.util.DB;

/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
@Alternative
public class MappedSSLUserList
	extends LinkedHashMap<String, UserListBean> implements UserList<UserListBean> {

	@Inject private transient Logger log;

	private static final long serialVersionUID = 1L;
	private static final String TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

	public static final String SQL_ZUSER =
			"select u.site_id, u.user_id, u.delete_flag, u.end_date"
			+ " from sas_prj_site_user u"
			+ " where u.user_id like 'Z%'"
//	証明書が有効なユーザに関する情報を取得する。その際、過去のPRJは考慮しない
//			+ " and u.delete_flag = '0'"
//	ユーザIDは一意で決定するので、orderを指定する必要はない　<- うそ
			+ " order by u.delete_flag"
	;

	public MappedSSLUserList() {
		super(4000);
	}

	// for envoronment not using weld-se
	public void init() {
		if (log == null) {
			// logのインスタンスが生成できないため
			log = Logger.getLogger(this.getClass().getName());
		}
	}

	@WithElaps
	public MappedSSLUserList load(String file, SiteList sitelist)
			throws IOException, ClassNotFoundException, SQLException
	{
		final Map<String, SelectUser> map = new HashMap<>();

		try ( // Oracleに接続
				Connection conn = DB.createConnection();
				// ステートメントを作成
				PreparedStatement stmt = conn.prepareStatement(SQL_ZUSER);
				ResultSet rs = stmt.executeQuery();
				)
		{
			final DateFormat f = new SimpleDateFormat(TIME_FORMAT);

			while (rs.next()) {
				String siteId = rs.getString(1);
				String userId = rs.getString(2);
				String userDelFlag = rs.getString(3);
				String endDate = rs.getTimestamp(4) == null ? "" : f.format(rs.getTimestamp(4));
				map.put(userId, new SelectUser(siteId, userId, userDelFlag, endDate));
			}
			rs.close();
			stmt.close();
			conn.close();
		}

		try (Stream<String> input = Files.lines(Paths.get(file), Charset.forName("utf-8"))) {

			input.filter(SSLIndexBean::test)
				.map(SSLIndexBean::parse)
				.filter(b -> b.getUserId().startsWith("Z"))	// index.txtの読み込みなので、SQLとは別の集合
				.forEach(b -> {
					UserListBean bean = this.get(b.getUserId());
					if (bean != null) {
						// 同じユーザIdが存在するということは、以前の証明書が失効している場合
						bean.update(b);
					}
					else {
						// DBから対象のユーザ情報を取得する
						SelectUser su = map.get(b.getUserId());
						if (su != null) {
							bean = new UserListBean(b);
							this.put(b.getUserId(), bean);

							SiteListIsp siteBean = sitelist.get(su.getSiteId());
							if (siteBean != null) {
								UserListSite site =
										new UserListSite(siteBean, su.getUserDelFlag(), su.getEndDate());
								bean.addSite(site);
							}
							else {
								log.log(Level.WARNING, "site is null: siteId={0}, bean=[{1}]",
										new Object[] { su.getSiteId(), bean });
							}
						}
						else {
							// DBから対象のユーザ情報を取得できなかった、つまり、
							// sslindexに存在するが、SSLテーブルに存在しない場合：
							// 不正な状態を検知することができるように削除フラグ"-1"でuserlistに追加する
							log.log(Level.WARNING, "user_id not found: sslindex=[{0}]", b);
							bean = new UserListBean(b);
							this.put(b.getUserId(), bean);
						}
					} 
				});
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

	class SelectUser {

		private final String siteId;
		private final String userId;
		private final String userDelFlag;
		private final String endDate;

		public SelectUser(String siteId, String userId, String userDelFlag, String endDate) {
			this.siteId = siteId;
			this.userId = userId;
			this.userDelFlag = userDelFlag;
			this.endDate = endDate;
		}

		public String getSiteId() {
			return siteId;
		}

		public String getUserId() {
			return userId;
		}

		public String getUserDelFlag() {
			return userDelFlag;
		}

		public String getEndDate() {
			return endDate;
		}

	}

}
