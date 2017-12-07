package logcheck.test;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.site.db.DbSiteList;
import logcheck.user.UserListBean;
import logcheck.user.ssl.SSLUserList;


/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
public class SSLUserListTest {

	@BeforeClass
	public static void beforeClass() {
		System.out.println("start SSLUserListTest ...");
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("SSLUserListTest ... end");
	}

	@Test
	public void test01() throws Exception {
		SSLUserList map = new SSLUserList();
		map.load(Env.SSLINDEX, new DbSiteList().load(null));
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

}
