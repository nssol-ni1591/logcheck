package logcheck.test;

import java.io.IOException;

import logcheck.sdc.SdcList;
import logcheck.sdc.SdcListIsp;
import logcheck.util.NetAddr;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SdcListTest {

	private static SdcList map;

	@BeforeClass
	public static void beforeClass() throws IOException {
		System.out.println("start SdcListTest ...");

		map = new SdcList();
		map.load(Env.SDCLIST);
		System.out.println("TsvSiteListTest.test01 size = " + map.size());
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("SdcListTest ... end");
	}
	@Before
	public void before() throws IOException {
	}

	@Test
	public void test01() {
		assertFalse(map.isEmpty());
		SdcListIsp isp = map.get(new NetAddr("172.30.88.1"));
		System.out.println("isp: " + isp);
	}
	@Test
	public void test02() {
		int ix = 0;
		//for (SdcListIsp isp : map.values()) {
		for (SdcListIsp isp : map) {
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
		assertEquals("sub segment", "基幹 軟件：武関(軟件小杉)", isp.getName());

		SdcListIsp isp2 = map.get(new NetAddr("172.30.90.65"));
		System.out.println("isp: " + isp);
		assertEquals("isp1 and ips1", isp, isp);
		assertEquals("isp2 and ips2", isp2, isp2);
		assertNotEquals("isp1 and ips2", isp, isp2);
		assertNotEquals("isp1 and abc", isp, "abc");
	}
	//@Test
	public void test05() {
		//map.values().forEach(System.out::println);
		map.forEach(System.out::println);
	}
	@Test
	public void test06() {
		SdcListIsp isp = map.get(new NetAddr("172.30.90.65"));
		System.out.println("isp: " + isp);
		//assertEquals("sub segment", "基幹 軟件：武関(軟件小杉)", isp.getName());
	}
}
