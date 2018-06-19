package logcheck.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.known.net.apnic.WhoisApnic;

public class WhoisApnicTest extends WhoisKnownListTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setupClass() throws Exception {
		System.out.println("start WhoisApnic ...");

		whois = new WhoisApnic();
		whois.init();
	}
	@AfterClass
	public static void afterClass() throws Exception {
		System.out.println("WhoisApnic ... end");
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
