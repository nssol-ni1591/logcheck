package logcheck.test;

import static org.junit.Assert.*;

import logcheck.fw.FwLogBean;
import logcheck.fw.FwLogSummary;
import logcheck.isp.Isp;
import logcheck.isp.IspBean;
import logcheck.util.net.NetAddr;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FwLogTest {

	@BeforeClass
	public static void beforeClass() {
		System.err.println("start FwLogTest ...");
	}
	@AfterClass
	public static void aforeClass() {
		System.err.println("FwLogTest ... end");
	}

	@Test
	public void test01() {
		FwLogBean b1 = new FwLogBean("2018-04-12", "00:00:45", "notice", "172.30.90.69", "43692", "128.221.236.246", "443");
		FwLogBean b2 = new FwLogBean("2018-04-13", "00:00:45", "notice", "172.30.90.69", "43692", "128.221.236.246", "443");
		
		assertEquals("match level", "notice", b1.getLevel());
		assertEquals("match SrcIp", "172.30.90.69", b1.getSrcIp().toString());
		assertEquals("match SrcPort", 43692, b1.getSrcPort());
		assertEquals("match DstIp", "128.221.236.246", b1.getDstIp().toString());
		assertEquals("match DstPort", 443, b1.getDstPort());
		System.out.println("b1: " + b1);

		// equals
		assertFalse(b1.equals(null));
		assertTrue(b1.equals(b1));
		assertTrue(b1.equals(b2));

		// compareTo
		assertTrue(b1.compareTo(b2) == 0);

		// srcportは無視
		b2 = new FwLogBean("2018-04-12", "00:00:45", "notice", "172.30.90.69", "0", "128.221.236.246", "443");
		assertTrue(b1.compareTo(b2) == 0);
		assertTrue(b1.equals(b2));

		// 大小比較
		b2 = new FwLogBean("2018-04-12", "00:00:45", "notice", "172.30.90.69", "43692", "128.221.236.246", "442");
		assertTrue(b1.compareTo(b2) < 0);
		assertFalse(b1.equals(b2));
		b2 = new FwLogBean("2018-04-12", "00:00:45", "notice", "172.30.90.69", "43692", "128.221.236.246", "444");
		assertTrue(b1.compareTo(b2) > 0);
		assertFalse(b1.equals(b2));
		b2 = new FwLogBean("2018-04-12", "00:00:45", "notice", "172.30.90.68", "43692", "128.221.236.246", "443");
		assertTrue(b1.compareTo(b2) < 0);
		assertFalse(b1.equals(b2));
		b2 = new FwLogBean("2018-04-12", "00:00:45", "notice", "172.30.90.70", "43692", "128.221.236.246", "443");
		assertTrue(b1.compareTo(b2) > 0);
		assertFalse(b1.equals(b2));
		b2 = new FwLogBean("2018-04-12", "00:00:45", "notice", "172.30.90.69", "43692", "128.221.236.245", "443");
		assertTrue(b1.compareTo(b2) < 0);
		assertFalse(b1.equals(b2));
		b2 = new FwLogBean("2018-04-12", "00:00:45", "notice", "172.30.90.69", "43692", "128.221.236.247", "443");
		assertTrue(b1.compareTo(b2) > 0);
		assertFalse(b1.equals(b2));
	}
	@Test
	public void test02() {
		FwLogBean b1 = new FwLogBean("2018-04-12", "00:00:45", "notice", "172.30.90.69", "43692", "128.221.236.246", "443");
		
		Isp isp1 = new IspBean<NetAddr>("ISP1", "JP", new NetAddr("172.30.90.69/24"));
		Isp isp2 = new IspBean<NetAddr>("ISP2", "JP", new NetAddr("128.221.236.246/24"));

		FwLogSummary sum1 = new FwLogSummary(b1, isp1, isp2);
		sum1.update("2018-04-30");

		// FWログは過去に流れていくので、update()はfirstDateを更新する
		assertEquals("match firstDate", "2018-04-30", sum1.getFirstDate());
		assertEquals("match lastDate", b1.getDate(), sum1.getLastDate());
		assertEquals("match srcAddr", b1.getSrcIp(), sum1.getSrcAddr());
		assertEquals("match srcAddr", b1.getDstIp(), sum1.getDstAddr());
		assertEquals("match srcIsp", isp1, sum1.getSrcIsp());
		assertEquals("match dstIsp", isp2, sum1.getDstIsp());
		assertEquals("match dstPort", b1.getDstPort(), sum1.getDstPort());

		assertTrue("compareTo == 0", sum1.compareTo(sum1) == 0);

		assertTrue("equlas self", sum1.equals(sum1));
		assertFalse("equlas null", sum1.equals(null));
		System.out.println("hashCode()=" + sum1.hashCode());
	}

}
