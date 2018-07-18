package logcheck.test;

import java.io.IOException;

import logcheck.sdc.SdcList;
import logcheck.sdc.SdcListIsp;
import logcheck.util.net.NetAddr;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * 以前の TsvMagListクラス
 */
public class SdcListTest {

	private SdcList map;

	@BeforeClass
	public static void beforeClass() {
		System.out.println("start SdcListTest ...");
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("SdcListTest ... end");
	}
	@Before
	public void before() throws IOException {
		map = new SdcList();
		map.load(Env.SDCLIST);
		System.out.println("TsvSiteListTest.test01 size = " + map.size());
	}

	@Test
	public void test01() {
		assertFalse(map.isEmpty());
		SdcListIsp isp = map.get(new NetAddr("172.30.88.0/24"));
		System.out.println("isp: " + isp);
	}
	@Test
	public void test02() {
		int ix = 0;
		for (SdcListIsp isp : map.values()) {
			isp.toString();
			assertFalse("equals(null) ... false", isp.equals(null));
			ix = ix + 1;
		}
	}
	@Test(expected = IOException.class)
	public void test03() throws IOException {
		SdcList map = new SdcList();
		map.load("Foo");
	}

	@Test
	public void test04() {
		SdcListIsp isp = map.get(new NetAddr("172.30.76.0/24"));
		System.out.println("isp: " + isp);
	}
	@Test
	public void test05() {
		map.values().forEach(System.out::println);
	}

}
