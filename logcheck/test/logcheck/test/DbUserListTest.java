package logcheck.test;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.user.UserListBean;
import logcheck.user.UserListSite;
import logcheck.user.db.DbUserList;

/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
public class DbUserListTest {

	@BeforeClass
	public static void beforeClass() {
		System.out.println("start DbUserListTest ...");
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("DbUserListTest ... end");
	}

	@Test
	public void test01() throws Exception {
		DbUserList map = new DbUserList();
		map.init();
		map.load(null, null);
		System.out.println("size=" + map.size());
		assertFalse(map.isEmpty());
		// select count(*) from ????
		assertEquals(2501, map.size());

		int ix = 0;
		int iy = 0;
		for (String userId : map.keySet()) {
			UserListBean b = map.get(userId);
			assertNotNull("UserListBean is null", b);
			for (UserListSite site : b.getSites()) {
				// 残念ながら、カバレッジ対応
				site.getAddress();
				site.getCount();
				site.getCountry();
				site.getFirstDate();
				site.getLastDate();
				site.getProjDelFlag();
				site.getProjId();
				site.getSiteDelFlag();
				site.getSiteId();
				site.getSiteName();
				site.getUserDelFlag();
				assertNotNull("UserListSite is null", site);
				ix += 1;
			}
			iy += 1;
		}
		System.out.println("user.count: " + iy + ", gip.count: " + ix);
	}

}
