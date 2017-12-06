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
		assertTrue("unmatch 0.0.0.0", "0.0.0.0/32 (0.0.0.0-0.0.0.0)".equals(addr.toStringRange()));
	}

}
