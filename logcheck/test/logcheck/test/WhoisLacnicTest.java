package logcheck.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.known.KnownListIsp;
import logcheck.known.net.html.WhoisLacnic;
import logcheck.util.net.ClientAddr;

public class WhoisLacnicTest extends WhoisKnownListTest {

	private static WhoisLacnic f;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setupWhoisTest() throws Exception {
		System.out.println("start WhoisLacnic ...");

		System.setProperty("proxySet" , "true");
		System.setProperty("proxyHost", "proxy.ns-sol.co.jp");
		System.setProperty("proxyPort", "8000");

		f = new WhoisLacnic();
		f.init();
	}
	@AfterClass
	public static void afterClass() throws Exception {
		System.out.println("WhoisLacnic ... end");
	}
	@Override
	protected KnownListIsp getIsp(String addr) {
		if (addr.isEmpty()) {
			return null;
		}

		KnownListIsp isp = f.get(new ClientAddr(addr));
		if (isp != null) {
			System.out.println("WhoisLacnic: addr=" + addr + ", isp=[" + isp + ", C=" + isp.getCountry() +", NET=" + isp.toStringNetwork() + "]");
		}
		else {
			System.out.println("WhoisLacnic: addr=" + addr + ", isp=null");
		}
		return isp;
	}

	@Test
	public void test01() {
		super.test01();
	}

}
