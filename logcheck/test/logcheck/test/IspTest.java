package logcheck.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.isp.Isp;
import logcheck.isp.IspBean;
import logcheck.isp.IspList;
import logcheck.isp.IspListImpl;
import logcheck.isp.IspMap;
import logcheck.isp.IspMap2;
import logcheck.util.ClientAddr;
import logcheck.util.NetAddr;

public class IspTest {
	@BeforeClass
	public static void beforeClass() {
		System.out.println("start IspMapTest ...");
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("IspMapTest ... end");
	}

	// Mapの比較するなんて、通常はナンセンスだ!!

	@Test
	public void ispbean() {
		Isp isp = new IspBean<>("name", "japan", "ref");
		assertEquals("ispbean.name", "name", isp.getName());
		assertEquals("ispbean.country", "japan", isp.getCountry());
		assertEquals("ispbean.tostring", isp.getName(), isp.toString());
		assertEquals("ispbean.hostname", "192.168.0.0", isp.getHostname(new NetAddr("192.168.0.0")));

		Isp isp2 = new IspBean<>("name", "jp", "ref");
		assertEquals("ispbean.country", "JP", isp2.getCountry());

		Isp isp3 = new IspBean<>("name", "japan", "ref");
		assertTrue("isp.compareTo(isp3) == 0", isp.compareTo(isp3) == 0);
		Isp isp4 = new IspBean<>("name1", "japan", "ref");
		assertTrue("isp.compareTo(isp4) < 0", isp.compareTo(isp4) < 0);
		Isp isp5 = new IspBean<>("name", "japal", "ref");
		assertTrue("isp.compareTo(isp5) > 0", isp.compareTo(isp5) > 0);
	}
	@Test
	public void isplist() {
		IspList isp = new IspListImpl("name", "japan");
		isp.addAddress(new NetAddr("192.168.0.0./24"));

		assertEquals("ispbean.name", "name", isp.getName());
		assertEquals("ispbean.country", "japan", isp.getCountry());
		isp.getAddress().forEach(addr -> {
			assertEquals("ispbean.getAddress", "192.168.0.0", addr.toString());
		});
		assertTrue("isplist.within", isp.within(new ClientAddr("192.168.0.1")));
		assertFalse("isplist.within", isp.within(new ClientAddr("192.168.1.1")));
	}
	@Test
	public void ispmap() {
		IspMap<String> ispmap1 = new IspMap<>();
		IspMap<String> ispmap2 = new IspMap<>("a", "b");
		IspMap<String> ispmap3 = new IspMap<>("a", "b");
		IspMap<String> ispmap4 = new IspMap<>(null, "b");

		assertFalse("ispmap1 == ispmap2", ispmap1.equals(ispmap2));
		assertFalse("ispmap2 == ispmap3", ispmap2.equals(ispmap3));
		assertFalse("ispmap2 == ispmap3", ispmap2.equals(ispmap4));
		assertEquals("getName() != a", "a", ispmap3.getName());
		assertEquals("getCountry() != b", "b", ispmap3.getCountry());
		System.out.println("hashCode: " + ispmap1.hashCode());
	}
	@Test
	public void ispmap2() {
		IspMap2<String> ispmap1 = new IspMap2<>();
		IspMap2<String> ispmap2 = new IspMap2<>("a", "b");
		IspMap2<String> ispmap3 = new IspMap2<>("a", "b");
		IspMap2<String> ispmap4 = new IspMap2<>(null, "b");

		assertFalse("ispmap1 == ispmap2", ispmap1.equals(ispmap2));
		assertTrue("ispmap2 == ispmap3", ispmap2.equals(ispmap3));
		assertFalse("ispmap2 == ispmap3", ispmap2.equals(ispmap4));
		assertEquals("getName() != a", "a", ispmap3.getName());
		assertEquals("getCountry() != b", "b", ispmap3.getCountry());
		System.out.println("hashCode: " + ispmap1.hashCode());

		ispmap1.setName("xxx");
		assertEquals("getName() != xxx", "xxx", ispmap1.getName());
		ispmap1.setCountry("yyy");
		assertEquals("getCountry() != yyy", "yyy", ispmap1.getCountry());
		ispmap1.setCountry("zz");
		assertEquals("getCountry() != ZZ", "ZZ", ispmap1.getCountry());
	}

}
