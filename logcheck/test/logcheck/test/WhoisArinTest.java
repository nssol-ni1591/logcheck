package logcheck.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.known.net.arin.WhoisArin;

public class WhoisArinTest extends WhoisKnownListTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setupClass() throws Exception {
		System.out.println("start WhoisArin ...");

		whois = new WhoisArin();
		whois.init();
	}
	@AfterClass
	public static void afterClass() throws Exception {
		System.out.println("WhoisArin ... end");
	}

	@Test
	public void test01() {
		super.test01();
	}
	@Test
	public void test02() {
		super.test02();
	}
}
