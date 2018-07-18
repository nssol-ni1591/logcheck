package logcheck.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.site.SiteListIsp;
import logcheck.site.db.DbSiteList;
import logcheck.util.net.NetAddr;


/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
public class DbSiteListTest {

	private static DbSiteList map;

	@BeforeClass
	public static void beforeClass() throws ClassNotFoundException, IOException, SQLException {
		System.out.println("start DbSiteListTest ...");

		map = new DbSiteList();
		map.init();
		map.load(null);
		System.out.println("size=" + map.size());
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("DbSiteListTest ... end");
	}
	@Before
	public void before() throws ClassNotFoundException, IOException, SQLException {
	}

	@Test
	public void test01() {
		assertFalse(map.isEmpty());
		// select count(*) from sas_prj_site_info;
		assertEquals(884, map.size());

		assertTrue(map.equals(map));
		assertFalse(map.equals(null));
		System.out.println("hashCode()=" + map.hashCode());
	}
	@Test
	public void test02() {
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
	@Test
	public void test03() {
		SiteListIsp isp = map.get("268");
		assertNotNull("site_id='268' not found", isp);
		String prjId = isp.getProjId();
		assertEquals("prj_id illegal", "PRJ_SDC_OM", prjId);
		isp.getName();
		isp.addAddress(new NetAddr("192.168.0.0/24"));

		System.out.println("isp: " + isp);
		System.out.println("hashCode()=" + isp.hashCode());
		assertFalse("compareTo > 0", isp.compareTo(null) > 0);
	}

}
