package logcheck.test;

import java.io.IOException;

import logcheck.site.SiteListIsp;
import logcheck.site.tsv.TsvSiteList;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * 以前の TsvMagListクラス
 */
public class TsvSiteListTest {

	@BeforeClass
	public static void beforeClass() {
		System.out.println("start TsvSiteListTest ...");
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("TsvSiteListTest ... end");
	}

	@Test
	public void test01() throws IOException {
		TsvSiteList map = new TsvSiteList();
		map.load("C:\\Users\\NI1591\\Desktop\\2017-セキュリティ対策\\xls\\maglist.txt");
		System.out.println("TsvSiteListTest.test01 size = " + map.size());
		assertFalse(map.isEmpty());

		int ix = 0;
		for (SiteListIsp isp : map.values()) {
			isp.getSiteId();
			isp.getAddress();
			ix = ix + 1;
		}
	}

	@Test(expected = IOException.class)
	public void test02() throws IOException {
		TsvSiteList map = new TsvSiteList();
		map.load("none");
		System.out.println("TsvSiteListTest.test02 size = " + map.size());
		assertTrue(map.isEmpty());
	}
}
