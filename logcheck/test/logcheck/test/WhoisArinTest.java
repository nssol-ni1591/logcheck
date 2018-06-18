package logcheck.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.known.KnownListIsp;
import logcheck.known.net.arin.WhoisArin;
import logcheck.util.net.ClientAddr;

public class WhoisArinTest extends WhoisKnownListTest {

	private static WhoisArin f;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setupWhoisTest() throws Exception {
		System.out.println("start WhoisArin ...");

		System.setProperty("proxySet" , "true");
		System.setProperty("proxyHost", "proxy.ns-sol.co.jp");
		System.setProperty("proxyPort", "8000");

//		weld = new Weld();
//		container = weld.initialize();
//		f = container.select(WhoisRest.class).get();
		f = new WhoisArin();
		f.init();
//		f.load(Env.KNOWNLIST);
	}
	@AfterClass
	public static void afterClass() throws Exception {
		System.out.println("WhoisArin ... end");
	}
	@Override
	protected KnownListIsp getIsp(String addr) {
		if (addr.isEmpty()) {
			return null;
		}

		KnownListIsp isp = f.get(new ClientAddr(addr));
		if (isp != null) {
			System.out.println("WhoisArin: addr=" + addr + ", isp=[" + isp + ", C=" + isp.getCountry() +", NET=" + isp.toStringNetwork() + "]");
		}
		else {
			System.out.println("WhoisArin: addr=" + addr + ", isp=null");
		}
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
