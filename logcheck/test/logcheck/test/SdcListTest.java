package logcheck.test;

import java.io.IOException;

import logcheck.sdc.SdcList;
import logcheck.sdc.SdcListIsp;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * 以前の TsvMagListクラス
 */
public class SdcListTest {

	@BeforeClass
	public static void beforeClass() {
		System.out.println("start SdcListTest ...");
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("SdcListTest ... end");
	}

	@Test
	public void test01() throws IOException {
		SdcList map = new SdcList();
		map.load("C:\\Users\\NI1591\\Desktop\\2017-セキュリティ対策\\xls\\sdclist.txt");
		System.out.println("TsvSiteListTest.test01 size = " + map.size());
		assertFalse(map.isEmpty());

		int ix = 0;
		for (SdcListIsp isp : map.values()) {
			isp.toString();
			assertFalse("equals(null) ... false", isp.equals(null));
			ix = ix + 1;
		}
	}

	@Test(expected = IOException.class)
	public void test02() throws IOException {
		SdcList map = new SdcList();
		map.load("Foo");
	}
}
