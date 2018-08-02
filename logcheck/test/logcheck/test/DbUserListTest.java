package logcheck.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.user.UserListBean;
import logcheck.user.UserListSite;
import logcheck.user.db.DbUserList;
import logcheck.util.net.NetAddr;

/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
public class DbUserListTest {

	private static DbUserList map;

	@BeforeClass
	public static void beforeClass() throws ClassNotFoundException, IOException, SQLException {
		System.out.println("start DbUserListTest ...");

		map = new DbUserList();
		map.init();
		map.load(null, null);
		System.out.println("size=" + map.size());
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("DbUserListTest ... end");
	}
	@Before
	public void before() throws ClassNotFoundException, IOException, SQLException {
	}
	
	@Test
	public void test01() {
		assertFalse(map.isEmpty());
		// select count(*) from ????
		assertEquals(2501, map.size());

		assertTrue(map.equals(map));
		assertFalse(map.equals(null));
		System.out.println("hashCode()=" + map.hashCode());
	}
	@Test
	public void test02() {
		UserListSite site = null;
		UserListBean bean = null;
		int ix = 0;
		int iy = 0;
		for (String userId : map.keySet()) {
			UserListBean b = map.get(userId);
			assertNotNull("UserListBean is null", b);
			bean = b;
			for (UserListSite s : b.getSites()) {
				site = s;
				ix += 1;
			}
			iy += 1;
		}
		System.out.println("user.count: " + iy + ", gip.count: " + ix);

		assertNotNull("UserListSite is null", site);
		assertNotNull("UserListBean is null", bean);

		assertNotNull("bean.getUserId() is not null", bean.getUserId());
		assertNotNull("bean.getValidFlag() is not null", bean.getValidFlag());
		assertNotNull("bean.getExpire() is not null", bean.getExpire());
		assertNotNull("bean.getRevoce() is not null", bean.getRevoce());
		System.out.println("hashCode()=" + bean.hashCode());

		assertNotNull("site.getAddress() is not null", site.getAddress());
		assertNotNull("site.getCount() is not null", site.getCount());
		assertNotNull("site.getCountry() is not null", site.getCountry());
		assertNotNull("site.getFirstDate() is not null", site.getFirstDate());
		assertNotNull("site.getLastDate() is not null", site.getLastDate());
		assertNotNull("site.getProjDelFlag() is not null", site.getProjDelFlag());
		assertNotNull("site.getProjId() is not null", site.getProjId());
		assertNotNull("site.getSiteDelFlag() is not null", site.getSiteDelFlag());
		assertNotNull("site.getSiteId() is not null", site.getSiteId());	// 正常に取得できること
		assertNotNull("site.getSiteName() is not null", site.getSiteName());
		assertNotNull("site.getUserDelFlag() is not null", site.getUserDelFlag());
		site.addAddress(new NetAddr("192.168.0.1/24"));
		System.out.println("hashCode()=" + site.hashCode());
	}
	@Test
	public void test03() {
		UserListBean b1 = new UserListBean("012345");
		UserListBean b2 = new UserListBean("123456");
		UserListBean b3 = new UserListBean("234567");
		UserListBean b4 = new UserListBean("123456");

		assertTrue("equals is true", b1.equals(b1));
		assertTrue("equals is true", b2.equals(b4));
		assertFalse("equals is false", b1.equals(b2));

		assertTrue("compareTo == 0", b2.compareTo(b2) == 0);
		assertTrue("compareTo < 0", b2.compareTo(b3) < 0);
		assertTrue("compareTo > 0", b2.compareTo(b1) > 0);

		System.out.println("b1: " + b1);
		System.out.println("hashCode()=" + b1.hashCode());
	}

}
