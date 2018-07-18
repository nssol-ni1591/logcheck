package logcheck.test;

import java.io.IOException;
import java.util.Optional;

import logcheck.sdc.SdcList;
import logcheck.sdc.SdcListIsp;
import logcheck.util.net.NetAddr;

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
	}
	//@Test
	public void test05() {
		//map.values().forEach(System.out::println);
		map.forEach(System.out::println);
	}
	@Test
	public void test06() {
		//Optional<SdcListIsp> rc = map.values().stream()
		Optional<SdcListIsp> rc = map.stream()
				.filter(isp -> isp.within(new NetAddr("172.30.76.0/24")))
				.findFirst();
		assertTrue(rc.isPresent());
		SdcListIsp isp = rc.get();
		System.out.println("isp: " + isp);
		assertEquals("sub segment", "基幹 軟件：武関(軟件小杉)", isp.getName());
	}
	/*
	@Test
	public void test07() {
		Optional<SdcListIsp> rc = map.keySet().stream()
				.map(key -> map.get(key))
				.filter(isp -> isp.within(new NetAddr("172.30.76.0/24")))
				.findFirst();
		assertTrue(rc.isPresent());
		SdcListIsp isp = rc.get();
		System.out.println("isp: " + isp);
		assertEquals("sub segment", "基幹 軟件：武関(軟件小杉)", isp.getName());
	}
	@Test
	public void test08() {
		Optional<SdcListIsp> rc = map.entrySet().stream()
				.map(e -> e.getValue())
				.filter(isp -> isp.within(new NetAddr("172.30.76.0/24")))
				.findFirst();
		assertTrue(rc.isPresent());
		SdcListIsp isp = rc.get();
		System.out.println("isp: " + isp);
		assertEquals("sub segment", "基幹 軟件：武関(軟件小杉)", isp.getName());
	}
*/
}
