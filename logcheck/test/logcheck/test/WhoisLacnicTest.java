package logcheck.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.known.impl.net.WhoisLacnic;

public class WhoisLacnicTest extends WhoisKnownListTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setupClass() throws Exception {
		System.out.println("start WhoisLacnic ...");

		whois = new WhoisLacnic();
		whois.init();
	}
	@AfterClass
	public static void afterClass() throws Exception {
		System.out.println("WhoisLacnic ... end");
	}

	@Test
	public void test01() {
		super.test01();
	}

}
