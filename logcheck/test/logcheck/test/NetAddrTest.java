package logcheck.test;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.util.net.NetAddr;

public class NetAddrTest {

	@BeforeClass
	public static void beforeClass() {
		System.out.println("start NetAddrTest ...");
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("NetAddrTest ... end");
	}

	@Test
	public void test01() {
		NetAddr addr = new NetAddr("0.0.0.0");
		assertTrue("unmatch 0.0.0.0", "0.0.0.0 (0.0.0.0-0.0.0.0)".equals(addr.toStringRange()));

		addr = new NetAddr("192.168.1.1", "192.168.1.0", "192.168.1.255");
		assertTrue("192.168.1.0/24".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.1.1", "192.168.1.0/16");
		assertTrue("192.168.1.1 (192.168.0.0-192.168.255.255)".equals(addr.toStringRange()));

		addr = new NetAddr("192.168.1.1", "192.168.1.0 - 192.168.1.255");
		assertTrue("192.168.1.0/24".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.1.1/16");
		assertTrue("192.168.0.0 (192.168.0.0-192.168.255.255)".equals(addr.toStringRange()));

		addr = new NetAddr("192.168.1.0 - 192.168.1.255");
		assertTrue("192.168.1.0/24".equals(addr.toStringNetwork()));
	}
	@Test
	public void test02() {
		NetAddr addr1 = new NetAddr("192.168.1.1");
		NetAddr addr2 = new NetAddr("192.168.1.1");
		NetAddr addr3 = new NetAddr("192.168.0.1");
		NetAddr addr4 = new NetAddr("192.168.2.1");
		assertTrue(addr1.equals(addr1));
		assertTrue(addr1.equals(addr2));
		assertFalse(addr1.equals(null));
		assertFalse(addr1.equals(addr3));
		assertFalse(addr1.equals(addr4));
	}
	@Test(expected = IllegalArgumentException.class)
	public void test03() {
		new NetAddr(null);
	}
	@Test(expected = NumberFormatException.class)
	public void test04() {
		new NetAddr("192.168.1.a");
	}


}
