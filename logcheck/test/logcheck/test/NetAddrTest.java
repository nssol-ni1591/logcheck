package logcheck.test;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.util.NetAddr;

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
		System.out.println("[test01]");
		NetAddr addr = new NetAddr("192.168.15.1", "192.168.15.0", "192.168.15.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/24".equals(addr.toStringNetwork()));
		int[] a = addr.getAddr();
		assertTrue(a[0] == 192 && a[1] == 168 && a[2] == 15 && a[3] == 1);
		assertTrue(addr.getNetmask() == 24);

		addr = new NetAddr("192.168.15.1", "192.168.15.0/16");
		System.out.println(addr.toStringRange());
		assertTrue("192.168.15.1 (192.168.0.0-192.168.255.255)".equals(addr.toStringRange()));

		addr = new NetAddr("192.168.15.1", "192.168.15.0 - 192.168.15.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/24".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.1/16");
		System.out.println(addr.toStringRange());
		assertTrue("192.168.0.0 (192.168.0.0-192.168.255.255)".equals(addr.toStringRange()));

		addr = new NetAddr("192.168.15.0 - 192.168.15.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/24".equals(addr.toStringNetwork()));
	}
	@Test
	public void test02() {
		NetAddr addr1 = new NetAddr("192.168.1.1");
		NetAddr addr2 = new NetAddr("192.168.1.1");
		NetAddr addr3 = new NetAddr("192.168.0.1");
		NetAddr addr4 = new NetAddr("192.168.2.1");
		assertTrue(addr1.equals(addr1));
		assertTrue(addr1.equals(addr2));
		//assertFalse(addr1.equals(null));
		assertFalse(addr1.equals(addr3));
		assertFalse(addr1.equals(addr4));
	}
	@Test
	public void test03() {
		NetAddr addr = new NetAddr("0.0.0.0");
		assertTrue("unmatch 0.0.0.0", "0.0.0.0 (0.0.0.0-0.0.0.0)".equals(addr.toStringRange()));
	}
	@Test
	public void test05() {
		System.out.println("[test05]");
		NetAddr addr = new NetAddr("192.168.15.0 - 192.168.15.3");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/30".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0 - 192.168.15.7");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/29".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0 - 192.168.15.15");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/28".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0 - 192.168.15.31");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/27".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0 - 192.168.15.63");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/26".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0 - 192.168.15.127");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/25".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0 - 192.168.15.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/24".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.14.0 - 192.168.15.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.14.0/23".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.12.0 - 192.168.15.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.12.0/22".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.8.0 - 192.168.15.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.8.0/21".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0 - 192.168.15.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/20".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0 - 192.168.31.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/19".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0 - 192.168.63.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/18".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0 - 192.168.127.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/17".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0 - 192.168.255.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/16".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0 - 192.169.255.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/15".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0 - 192.171.255.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/14".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0 - 192.175.255.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/13".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.160.0.0 - 192.175.255.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.160.0.0/12".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.160.0.0 - 192.191.255.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.160.0.0/11".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.128.0.0 - 192.191.255.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.128.0.0/10".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.128.0.0 - 192.255.255.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.128.0.0/9".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.0.0.0 - 192.255.255.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.0.0.0/8".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.0.0.0 - 193.255.255.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.0.0.0/7".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.0.0.0 - 195.255.255.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.0.0.0/6".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.0.0.0 - 199.255.255.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.0.0.0/5".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.0.0.0 - 207.255.255.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.0.0.0/4".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.0.0.0 - 223.255.255.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.0.0.0/3".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.0.0.0 - 255.255.255.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.0.0.0/2".equals(addr.toStringNetwork()));

		addr = new NetAddr("128.0.0.0 - 255.255.255.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("128.0.0.0/1".equals(addr.toStringNetwork()));
	}
	@Test
	public void test06() {
		System.out.println("[test06]");
		NetAddr addr = new NetAddr("192.168.15.0 - 192.168.15.2");
		System.out.println(addr.toStringNetwork());
		//assertTrue("192.168.15.0/24".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0 - 192.168.15.4");
		System.out.println(addr.toStringNetwork());
		//assertTrue("192.168.15.0/24".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0 - 192.168.15.8");
		System.out.println(addr.toStringNetwork());
		//assertTrue("192.168.15.0/24".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0 - 192.168.15.16");
		System.out.println(addr.toStringNetwork());
		//assertTrue("192.168.15.0/24".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0 - 192.168.15.32");
		System.out.println(addr.toStringNetwork());
		//assertTrue("192.168.15.0/24".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0 - 192.168.15.64");
		System.out.println(addr.toStringNetwork());
		//assertTrue("192.168.15.0/24".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0 - 192.168.15.128");
		System.out.println(addr.toStringNetwork());
		//assertTrue("192.168.15.0/24".equals(addr.toStringNetwork()));
	}
	@Test
	public void test07() {
		System.out.println("[test07]");
		NetAddr addr = new NetAddr("10.0.0.0/8");
		System.out.println(addr.toStringNetwork());
		//assertTrue("192.168.15.0/24".equals(addr.toStringNetwork()));

		addr = new NetAddr("10.0.0.0/18");
		System.out.println(addr.toStringNetwork());
		//assertTrue("192.168.15.0/24".equals(addr.toStringNetwork()));
	}
	@Test
	public void test08() {
		System.out.println("[test08]");
		NetAddr addr = new NetAddr("192.168.15.0/255.255.255.252");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/30".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0/255.255.255.248");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/29".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0/255.255.255.240");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/28".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0/255.255.255.224");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/27".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0/255.255.255.192");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/26".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0/255.255.255.128");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/25".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0/255.255.255.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/24".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.14.0/255.255.254.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.14.0/23".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.12.0/255.255.252.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.12.0/22".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.8.0/255.255.248.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.8.0/21".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0/255.255.240.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/20".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0/255.255.224.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/19".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0/255.255.192.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/18".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0/255.255.128.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/17".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0/255.255.0.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/16".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0/255.254.0.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/15".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0/255.252.0.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/14".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0/255.248.0.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/13".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.160.0.0/255.240.0.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.160.0.0/12".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.160.0.0/255.224.0.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.160.0.0/11".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.128.0.0/255.192.0.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.128.0.0/10".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.128.0.0/255.128.0.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.128.0.0/9".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.0.0.0/255.0.0.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.0.0.0/8".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.0.0.0/254.0.0.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.0.0.0/7".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.0.0.0/252.0.0.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.0.0.0/6".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.0.0.0/248.0.0.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.0.0.0/5".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.0.0.0/240.0.0.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.0.0.0/4".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.0.0.0/224.0.0.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.0.0.0/3".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.0.0.0/192.0.0.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.0.0.0/2".equals(addr.toStringNetwork()));

		addr = new NetAddr("128.0.0.0/128.0.0.0");
		System.out.println(addr.toStringNetwork());
		assertTrue("128.0.0.0/1".equals(addr.toStringNetwork()));
	}
	@Test
	public void test09() {
		System.out.println("[test09]");
		NetAddr addr = new NetAddr("192.168.15.32/27");
		System.out.println(addr.toStringRange());
		assertTrue(addr.within(new NetAddr("192.168.15.33")));
		assertTrue(addr.within(new NetAddr("192.168.15.63")));
		assertFalse(addr.within(new NetAddr("192.168.15.31")));
		assertFalse(addr.within(new NetAddr("192.168.15.64")));
		assertFalse(addr.within(new NetAddr("191.168.15.33")));
		assertFalse(addr.within(new NetAddr("192.167.15.63")));
		assertFalse(addr.within(new NetAddr("191.168.15.33")));
		assertFalse(addr.within(new NetAddr("192.169.15.63")));
		//assertFalse(addr.within(null));

		assertTrue(addr.compareTo(new NetAddr("192.168.15.32 - 192.168.15.63")) == 0);
		assertTrue(addr.compareTo(new NetAddr("192.168.15.0 - 192.168.15.31")) < 0);
		assertTrue(addr.compareTo(new NetAddr("192.168.15.64 - 192.168.16.127")) > 0);
		assertTrue(addr.compareTo(new NetAddr("192.168.15.32 - 192.168.15.47")) < 0);
	}

	@Test(expected = NullPointerException.class)
	public void test11() {
		new NetAddr(null);
	}
	@Test(expected = NumberFormatException.class)
	public void test12() {
		new NetAddr("192.168.1.a");
	}
	@Test(expected = IllegalArgumentException.class)
	public void test13() {
		new NetAddr("192.168.15.0/255.255.253.0");
	}
	@Test(expected = IllegalArgumentException.class)
	public void test14() {
		new NetAddr("192.168.15.0//255.255.255.0");
	}
	@Test(expected = IllegalArgumentException.class)
	public void test15() {
		new NetAddr("192.168.15.0/255.255.255");
	}
	@Test(expected = IllegalArgumentException.class)
	public void test16() {
		NetAddr addr = new NetAddr("192.168.15.1.1", "192.168.15.0", "192.168.15.255");
		System.out.println(addr.toStringRange());
	}
	@Test(expected = NullPointerException.class)
	public void test17() {
		NetAddr addr = new NetAddr("192.168.15.1", null, "192.168.15.255");
		System.out.println(addr.toStringNetwork());
	}

}
