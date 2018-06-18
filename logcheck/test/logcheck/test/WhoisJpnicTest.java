package logcheck.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.known.KnownListIsp;
import logcheck.known.net.jpnic.WhoisJpnic;
import logcheck.util.net.ClientAddr;

public class WhoisJpnicTest extends WhoisKnownListTest {

	private static WhoisJpnic f;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setupWhoisTest() throws Exception {
		System.out.println("start WhoisJpnic ...");

		System.setProperty("proxySet" , "true");
		System.setProperty("proxyHost", "proxy.ns-sol.co.jp");
		System.setProperty("proxyPort", "8000");

//		weld = new Weld();
//		container = weld.initialize();
//		f = container.select(WhoisRest.class).get();
		f = new WhoisJpnic();
		f.init();
//		f.load(Env.KNOWNLIST);
	}
	@AfterClass
	public static void afterClass() throws Exception {
		System.out.println("WhoisJpnic ... end");
	}
	@Override
	protected KnownListIsp getIsp(String addr) {
		if (addr.isEmpty()) {
			return null;
		}

		KnownListIsp isp = f.get(new ClientAddr(addr));
		if (isp != null) {
			System.out.println("WhoisJpnic: addr=" + addr + ", isp=[" + isp + ", C=" + isp.getCountry() +", NET=" + isp.toStringNetwork() + "]");
		}
		else {
			System.out.println("WhoisJpnic: addr=" + addr + ", isp=null");
		}
		return isp;
	}

	@Test
	public void test01() {
//		super.test01();
	}
	@Test
	public void test02() {
//		super.test02();
	}
	@Test
	public void test03() {
//		super.test03();
	}
	@Test
	public void test04() {
//		super.test04();
	}
	@Test
	public void test05() {
//		super.test05();
	}
	@Test
	public void test06() {
//		super.test06();
	}
	@Test
	public void test07() {
//		super.test07();
	}
	@Test
	public void test08() {
//		super.test08();
	}
	@Test
	public void test09() {
//		super.test09();
	}
	@Test
	public void test10() {
//		super.test10();
	}

}
