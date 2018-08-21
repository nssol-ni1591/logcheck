package logcheck.test;

import java.io.IOException;

import logcheck.site.SiteListIsp;
import logcheck.site.impl.TsvSiteList;
import logcheck.site.impl.TsvSiteListBean;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * 以前の TsvMagListクラス
 */
public class TsvSiteListTest {

	private static TsvSiteList map;

	@BeforeClass
	public static void beforeClass() throws IOException {
		System.out.println("start TsvSiteListTest ...");

		map = new TsvSiteList();
		map.init();
		map.load(Env.MAGLIST);
		System.out.println("TsvSiteListTest.test01 size = " + map.size());
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("TsvSiteListTest ... end");
	}
	@Before
	public void before() throws IOException {
	}

	@Test
	public void test01() {
		assertFalse(map.isEmpty());
		//map.values().forEach(System.out::println);
		assertFalse("equals(null) is true", map.equals(null));
		System.out.println("hashCode: " + map.hashCode());
	}
	@Test
	public void test02() {
		SiteListIsp isp = map.get("PRJ_SDC_OM");
		System.out.println("isp=" + isp);

		System.out.println("isp.getSiteName(): " + isp.getSiteName());
		System.out.println("isp.getAddress(): " + isp.getAddress());
		System.out.println("isp.hashCode(): " + isp.hashCode());

		assertEquals("match projId", "PRJ_SDC_OM", isp.getProjId());
		assertEquals("match siteId", "", isp.getSiteId());
		assertEquals("match projId and name", isp.getProjId(), isp.getName());
		assertEquals("match projDelFlag", "0", isp.getProjDelFlag());
		assertEquals("match siteDelFlag", "0", isp.getSiteDelFlag());

		assertFalse("equals(null) is true", isp.equals(null));
		assertNotNull("isp#toString is null", isp.getAddress());
	}
	@Test(expected = IOException.class)
	public void test03() throws Exception {
		TsvSiteList map = new TsvSiteList();
		map.init();
		map.load("abc.txt");
	}
	@Test
	public void test04() {
		TsvSiteListBean b1 = new TsvSiteListBean("PRJ_1234", "1234", "SITE_1234", "192.168.0.1", "255.255.255.0");
		assertEquals("match projId", "PRJ_1234", b1.getProjId());
		assertEquals("match projName", "1234", b1.getProjName());
		assertEquals("match projSiteName", "SITE_1234", b1.getSiteName());
		assertEquals("match ip address", "192.168.0.1/255.255.255.0", b1.getMagIp());
	}
	@Test(expected = IllegalArgumentException.class)
	public void test05() throws Exception {
		new TsvSiteListBean("PRJ_1234", "1234", "SITE_1234", "192.168.0.1/24", "255.255.255.0");
	}

}
