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
		System.out.println("[test01]");
		NetAddr addr = new NetAddr("0.0.0.0");
		assertTrue("unmatch 0.0.0.0", "0.0.0.0 (0.0.0.0-0.0.0.0)".equals(addr.toStringRange()));

		addr = new NetAddr("192.168.1.1", "192.168.1.0", "192.168.1.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.1.0/24".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.1.1", "192.168.1.0/16");
		System.out.println(addr.toStringRange());
		assertTrue("192.168.1.1 (192.168.0.0-192.168.255.255)".equals(addr.toStringRange()));

		addr = new NetAddr("192.168.1.1", "192.168.1.0 - 192.168.1.255");
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.1.0/24".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.1.1/16");
		System.out.println(addr.toStringRange());
		assertTrue("192.168.0.0 (192.168.0.0-192.168.255.255)".equals(addr.toStringRange()));

		addr = new NetAddr("192.168.1.0 - 192.168.1.255");
		System.out.println(addr.toStringNetwork());
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
	@Test
	public void test05() {
		System.out.println("[test05]");
		NetAddr addr = new NetAddr("192.168.15.0 - 192.168.15.3");	// /30
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/30".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0 - 192.168.15.7");	// /29
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/29".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0 - 192.168.15.15");	// /28
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/28".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0 - 192.168.15.31");	// /27
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/27".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0 - 192.168.15.63");	// /26
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/26".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0 - 192.168.15.127");	// /25
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/25".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.15.0 - 192.168.15.255");	// /24
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.15.0/24".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.14.0 - 192.168.15.255");	// /23
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.14.0/23".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.12.0 - 192.168.15.255");	// /22
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.12.0/22".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.8.0 - 192.168.15.255");	// /21
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.8.0/21".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0 - 192.168.15.255");	// /20
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/20".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0 - 192.168.31.255");	// /19
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/19".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0 - 192.168.63.255");	// /18
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/18".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0 - 192.168.127.255");	// /17
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/17".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0 - 192.168.255.255");	// /16
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/16".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0 - 192.169.255.255");	// /15
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/15".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0 - 192.171.255.255");	// /14
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/14".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.168.0.0 - 192.175.255.255");	// /13
		System.out.println(addr.toStringNetwork());
		assertTrue("192.168.0.0/13".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.160.0.0 - 192.175.255.255");	// /12
		System.out.println(addr.toStringNetwork());
		assertTrue("192.160.0.0/12".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.160.0.0 - 192.191.255.255");	// /11
		System.out.println(addr.toStringNetwork());
		assertTrue("192.160.0.0/11".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.128.0.0 - 192.191.255.255");	// /10
		System.out.println(addr.toStringNetwork());
		assertTrue("192.128.0.0/10".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.128.0.0 - 192.255.255.255");	// /9
		System.out.println(addr.toStringNetwork());
		assertTrue("192.128.0.0/9".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.0.0.0 - 192.255.255.255");	// /8
		System.out.println(addr.toStringNetwork());
		assertTrue("192.0.0.0/8".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.0.0.0 - 193.255.255.255");	// /7
		System.out.println(addr.toStringNetwork());
		assertTrue("192.0.0.0/7".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.0.0.0 - 195.255.255.255");	// /6
		System.out.println(addr.toStringNetwork());
		assertTrue("192.0.0.0/6".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.0.0.0 - 199.255.255.255");	// /5
		System.out.println(addr.toStringNetwork());
		assertTrue("192.0.0.0/5".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.0.0.0 - 207.255.255.255");	// /4
		System.out.println(addr.toStringNetwork());
		assertTrue("192.0.0.0/4".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.0.0.0 - 223.255.255.255");	// /3
		System.out.println(addr.toStringNetwork());
		assertTrue("192.0.0.0/3".equals(addr.toStringNetwork()));

		addr = new NetAddr("192.0.0.0 - 255.255.255.255");	// /2
		System.out.println(addr.toStringNetwork());
		assertTrue("192.0.0.0/2".equals(addr.toStringNetwork()));

		addr = new NetAddr("128.0.0.0 - 255.255.255.255");	// /1
		System.out.println(addr.toStringNetwork());
		assertTrue("128.0.0.0/1".equals(addr.toStringNetwork()));

	}

}
