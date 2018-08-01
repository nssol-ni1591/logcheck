package logcheck.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.isp.IspMap;
import logcheck.isp.IspMap2;

public class IspMapTest {
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
	}

}
