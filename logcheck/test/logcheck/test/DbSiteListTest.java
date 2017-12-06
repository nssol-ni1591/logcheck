package logcheck.test;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.site.SiteListIsp;
import logcheck.site.db.DbSiteList;


/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
public class DbSiteListTest {

	@BeforeClass
	public static void beforeClass() {
		System.out.println("start DbSiteListTest ...");
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("DbSiteListTest ... end");
	}

	@Test
	public void test() throws Exception {
		DbSiteList map = new DbSiteList();
		map.load(null);
		System.out.println("size=" + map.size());
		assertFalse(map.isEmpty());

		int ix = 0;
		for (SiteListIsp isp : map.values()) {
			assertNotNull("getCountry() is null", isp.getCountry());
			assertNotNull("getName() is null", isp.getName());
			assertNotNull("getSiteId() is null", isp.getSiteId());
			assertNotNull("getAddress() is null", isp.getAddress());
			assertFalse("equlas(null)", isp.equals(null));
			ix += 1;
		}
		System.out.println("count: " + ix);
	}

}
