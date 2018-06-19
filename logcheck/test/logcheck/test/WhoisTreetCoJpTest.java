package logcheck.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.known.net.html.WhoisTreetCoJp;

public class WhoisTreetCoJpTest extends WhoisKnownListTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setupClass() throws Exception {
		System.out.println("start WhoisTreetCoJp ...");

		whois = new WhoisTreetCoJp();
		whois.init();
	}
	@AfterClass
	public static void afterClass() throws Exception {
		System.out.println("WhoisTreetCoJp ... end");
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
