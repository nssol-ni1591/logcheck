package logcheck.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.site.db.DbSiteList;
import logcheck.user.UserListBean;
import logcheck.user.ssl.MappedSSLUserList;


/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
public class MappedSSLUserListTest {

	private static MappedSSLUserList map;

	@BeforeClass
	public static void beforeClass() throws ClassNotFoundException, IOException, SQLException {
		System.out.println("start MappedSSLUserListTest ...");

		DbSiteList site = new DbSiteList();
		site.init();
		site.load(null);

		map = new MappedSSLUserList();
		map.init();
		map.load(Env.SSLINDEX, site);
		System.out.println("size=" + map.size());
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("MappedSSLUserListTest ... end");
	}
	@Before
	public void before() throws ClassNotFoundException, IOException, SQLException {
	}
	@Test
	public void test01() {
		assertFalse(map.isEmpty());
		assertFalse(map.equals(null));
		System.out.println("hashCode()=" + map.hashCode());
	}
	@Test
	public void test02() {
		UserListBean user = null;
		for (UserListBean u : map.values()) {
			user = u;
		}
		assertFalse(user == null);
		assertNotNull("getUserId() is null", user.getUserId());
		assertNotNull("getValidFlag() is null", user.getValidFlag());
		assertNotNull("getExpire() is null", user.getExpire());
		assertNotNull("getRevoce() is null", user.getRevoce());
		assertNotNull("getTotal() is null", user.getTotal());
		assertNotNull("getProjDelFlag() is null", user.getProjDelFlag());
		assertNotNull("getSiteDelFlag() is null", user.getSiteDelFlag());
		assertNotNull("getUserDelFlag() is null", user.getUserDelFlag());
		assertNotNull("getFirstDate() is null", user.getFirstDate());
		assertNotNull("getLastDate() is null", user.getLastDate());

		assertTrue("equals same object", user.equals(user));
		//assertFalse("equals null", user.equals(null));
		System.out.println("user=" + user.toString());
	}
	@Test
	public void test03() throws Exception {
		MappedSSLUserList map = new MappedSSLUserList();
		assertTrue(map.isEmpty());
	}

}
