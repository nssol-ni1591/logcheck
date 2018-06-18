package logcheck.test;

import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.known.KnownListIsp;
import logcheck.known.net.apnic.WhoisApnic;
import logcheck.util.net.ClientAddr;

public class WhoisApnicTest extends WhoisKnownListTest {

	private static WhoisApnic f;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setupWhoisTest() throws Exception {
		System.out.println("start WhoisApnic ...");

		System.setProperty("proxySet" , "true");
		System.setProperty("proxyHost", "proxy.ns-sol.co.jp");
		System.setProperty("proxyPort", "8000");

//		weld = new Weld();
//		container = weld.initialize();
//		f = container.select(WhoisRest.class).get();
		f = new WhoisApnic();
		f.init();
//		f.load(Env.KNOWNLIST);
	}
	@AfterClass
	public static void afterClass() throws Exception {
		System.out.println("WhoisApnic ... end");
	}
	@Override
	protected KnownListIsp getIsp(String addr) {
		if (addr.isEmpty()) {
			return null;
		}

		KnownListIsp isp = f.get(new ClientAddr(addr));
		assertNotNull(isp);
		System.out.println("WhoisApnic: addr=" + addr + ", isp=[" + isp + ", C=" + isp.getCountry() +", NET=" + isp.toStringNetwork() + "]");
		return isp;
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
