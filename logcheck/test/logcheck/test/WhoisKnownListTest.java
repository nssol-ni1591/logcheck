package logcheck.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.known.KnownListIsp;
import logcheck.known.net.WhoisKnownList;
import logcheck.util.net.ClientAddr;


public class WhoisKnownListTest {

	private static Weld weld;
	private static WeldContainer container;
	private static WhoisKnownList f;
	private boolean isCheck = false;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setupWhoisTest() throws Exception {
		System.out.println("start WhoisKnownList ...");

		System.setProperty("proxySet" , "true");
		System.setProperty("proxyHost", "proxy.ns-sol.co.jp");
		System.setProperty("proxyPort", "8000");

		weld = new Weld();
		container = weld.initialize();
		f = container.select(WhoisKnownList.class).get();
//		f = new WhoisKnownList();
//		f.init();
//		f.load(Env.KNOWNLIST);
	}
	@AfterClass
	public static void afterClass() throws Exception {
		System.out.println("WhoisKnownList ... end");
	}

	@Before
	public void beforeInstance() throws Exception {
		String name = this.getClass().getSimpleName();
		if (name != null && name.equals("WhoisKnownListTest")) {
			isCheck = true;
		}
	}

	protected KnownListIsp getIsp(String addr) {
		if (addr.isEmpty()) {
			return null;
		}

		KnownListIsp isp = f.get(new ClientAddr(addr));
		if (isp != null) {
			System.out.println("WhoisKnownList: addr=" + addr + ", isp=[" + isp + ", C=" + isp.getCountry() +", NET=" + isp.toStringNetwork() + "]");
		}
		else {
			System.out.println("WhoisKnownList: addr=" + addr + ", isp=null");
		}
		return isp;
	}

	@Test
	public void test01() {
		getIsp("");
		getIsp("182.52.109.69");
	}
	@Test
	public void test02() {
		getIsp("");
	}
	@Test
	public void test03() {
		// キャッシュ
		KnownListIsp isp1 = getIsp("210.173.87.154");
		KnownListIsp isp2 = getIsp("210.173.87.156");
		assertNotNull(isp1);
		assertNotNull(isp2);
		if (isCheck) {
			assertEquals(isp1.getName(), isp2.getName());
			assertEquals(isp1.getAddress(), isp2.getAddress());
		}
	}
	@Test
	public void test04() {
		// apnic: replace descr
		//getIsp("1.115.195.230");

		// Amazon
		getIsp("54.89.92.4");
		getIsp("52.192.191.92");
		
		// Digital Ocean
		getIsp("104.236.241.73");
//		getIsp("104.131.183.115");
		getIsp("138.197.18.68");
		getIsp("45.55.0.0");
	}
	@Test
	public void test05() {
		// delete "(.*)" at the end of the line
		Pattern p = Pattern.compile("\\(.*\\)$");
		Matcher m;

		KnownListIsp isp = getIsp("64.134.171.160");
		assertNotNull(isp);
		if (isCheck) {
			m = p.matcher(isp.getName());
			assertFalse(m.find());
		}

		isp = getIsp("216.58.220.238");
		assertNotNull(isp);
		if (isCheck) {
			m = p.matcher(isp.getName());
			assertFalse(m.find());
		}
	}
	@Test
	public void test06() {
		// null address
		KnownListIsp isp = getIsp("260.1.1.1");
		assertTrue(isp == null || isp.getName() == null || isp.getName().equals("260.1.1.1"));
	}
	@Test
	public void test07() {
		// from knownfile
		KnownListIsp isp = getIsp("93.144.39.100");
		assertNotNull(isp);
		if (isCheck) {
			assertEquals("Vodafone DSL Italy", isp.getName());
		}

		isp = getIsp("151.0.141.74");
		assertNotNull(isp);
		if (isCheck) {
			assertEquals("Fastweb SpA", isp.getName());
		}

		isp = getIsp("51.38.12.13");
		assertNotNull(isp);
		if (isCheck) {
			assertEquals("OVH SAS", isp.getName());
		}
	}
	@Test
	public void test08() {
		// 上位プロバイダ[ARTERIA Networks Corporation] が存在するアドレス
		KnownListIsp isp = getIsp("113.33.234.133");
		assertNotNull(isp);
		if (isCheck) {
			assertEquals("NS Solutions Corporation", isp.getName());
		}

		isp = getIsp("124.35.68.168");
		assertNotNull(isp);
		if (isCheck) {
			assertEquals("MOL Information Systems, Ltd.", isp.getName());
		}
	}
	@Test
	public void test09() {
		// apnic not found
		// Arin only (Amazon JP)
		KnownListIsp isp = getIsp("54.255.148.42");
		assertNotNull(isp);
		if (isCheck) {
			assertEquals("Amazon Technologies Inc.", isp.getName());
		}

		// Organization -> CustName hit
		isp = getIsp("71.6.146.185");
		assertNotNull(isp);
		if (isCheck) {
			assertEquals("CariNet, Inc.", isp.getName());
		}
	}
	@Test
	public void test10() {
		// lacnic
		KnownListIsp isp = getIsp("201.6.0.0");
		if (isCheck) {
			assertNotEquals("--", isp.getCountry());
		}
		isp = getIsp("187.95.130.0");
		if (isCheck) {
			assertNotEquals("--", isp.getCountry());
		}

		// ripe
		isp = getIsp("139.162.106.181");
		if (isCheck) {
			assertNotEquals("--", isp.getCountry());
		}
	}
}
