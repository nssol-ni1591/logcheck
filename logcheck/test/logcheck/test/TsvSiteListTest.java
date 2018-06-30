package logcheck.test;

import java.io.IOException;

import logcheck.site.SiteListIsp;
import logcheck.site.tsv.TsvSiteList;

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
}
