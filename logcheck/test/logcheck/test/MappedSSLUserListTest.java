package logcheck.test;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.site.db.DbSiteList;
import logcheck.user.UserListBean;
import logcheck.user.ssl.MappedSSLUserList;


/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
public class MappedSSLUserListTest {

	@BeforeClass
	public static void beforeClass() {
		System.out.println("start MappedSSLUserListTest ...");
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("MappedSSLUserListTest ... end");
	}

	@Test
	public void test01() throws Exception {
		DbSiteList site = new DbSiteList();
		site.init();
		site.load(null);

		MappedSSLUserList map = new MappedSSLUserList();
		map.init();
		map.load(Env.SSLINDEX, site);
		System.out.println("size=" + map.size());
		assertFalse(map.isEmpty());

		for (UserListBean u : map.values()) {
			assertNotNull("getUserId() is null", u.getUserId());
			assertNotNull("getValidFlag() is null", u.getValidFlag());
			assertNotNull("getExpire() is null", u.getExpire());
			assertNotNull("getRevoce() is null", u.getRevoce());
			assertNotNull("getTotal() is null", u.getTotal());
			assertNotNull("getProjDelFlag() is null", u.getProjDelFlag());
			assertNotNull("getSiteDelFlag() is null", u.getSiteDelFlag());
			assertNotNull("getUserDelFlag() is null", u.getUserDelFlag());
			assertNotNull("getFirstDate() is null", u.getFirstDate());
			assertNotNull("getLastDate() is null", u.getLastDate());
			assertFalse("equals(null)", u.equals(null));
		}
	}

	@Test
	public void test02() throws Exception {
		MappedSSLUserList map = new MappedSSLUserList();
		assertTrue(map.isEmpty());
	}

}
