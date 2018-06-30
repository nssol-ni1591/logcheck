package logcheck.test;

import java.io.IOException;

import logcheck.site.SiteListIsp;
import logcheck.site.tsv.TsvSiteList;
import logcheck.site.tsv.TsvSiteListBean;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * 以前の TsvMagListクラス
 */
public class TsvSiteListTest {

	private TsvSiteList map;

	@BeforeClass
	public static void beforeClass() {
		System.out.println("start TsvSiteListTest ...");
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("TsvSiteListTest ... end");
	}
	@Before
	public void before() throws IOException {
		map = new TsvSiteList();
		map.init();
		map.load(Env.MAGLIST);
		System.out.println("TsvSiteListTest.test01 size = " + map.size());
	}

	@Test
	public void test01() {
		assertFalse(map.isEmpty());
	}
	@Test
	public void test02() {
		int ix = 0;
		for (SiteListIsp isp : map.values()) {
			isp.getSiteId();
			isp.getName();
			isp.getAddress();
			isp.toString();
			assertFalse("equals(null) is true", isp.equals(null));
			assertNotNull("isp#toString is null", isp.getAddress());
			ix = ix + 1;
		}
	}
	@Test(expected = IOException.class)
	public void test03() throws Exception {
		TsvSiteList map = new TsvSiteList();
		map.init();
		map.load("none");
	}
	@Test
	public void test04() {
		TsvSiteListBean b1 = new TsvSiteListBean("PRJ_1234", "1234", "SITE_1234", "192.168.0.1", "255.255.255.0");
		assertEquals("match projId", "PRJ_1234", b1.getProjId());
		assertEquals("match projName", "1234", b1.getProjName());
		assertEquals("match projSiteName", "SITE_1234", b1.getSiteName());
		assertEquals("match ip address", "192.168.0.1", b1.getMagIp());
	}

}
