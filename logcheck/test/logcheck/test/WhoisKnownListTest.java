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

import logcheck.known.KnownList;
import logcheck.known.KnownListIsp;
import logcheck.known.net.Whois;
import logcheck.known.net.WhoisKnownList;
import logcheck.util.Constants;
import logcheck.util.net.ClientAddr;

public class WhoisKnownListTest {

	private static Weld weld;
	private static WeldContainer container;

	private static KnownList knownlist;
	protected static Whois whois;

	protected String name;
	protected boolean check = false;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setupClass() throws Exception {
		System.out.println("start WhoisKnownListTest ...");

		weld = new Weld();
		container = weld.initialize();
		knownlist = container.select(WhoisKnownList.class).get();
		knownlist.init();
		whois = null;
	}
	@BeforeClass
	public static void setProxy() {
		Env.init();
	}
	@AfterClass
	public static void afterClass() throws Exception {
		System.out.println("WhoisKnownListTest ... end");
	}

	@Before
	public void before() throws Exception {
		name = this.getClass().getSimpleName();
		if (name.equals("WhoisKnownListTest")) {
			check = true;
		}
	}

	protected KnownListIsp getIsp(String addr) {
		if (addr.isEmpty()) {
			return null;
		}

		KnownListIsp isp = whois == null ? knownlist.get(new ClientAddr(addr)) : whois.get(new ClientAddr(addr));
		if (isp != null) {
			System.out.println(name + ": addr=" + addr + ", isp=[" + isp + ", C=" + isp.getCountry() +", NET=" + isp.toStringNetwork() + "]");
		}
		else {
			System.out.println(name + ": addr=" + addr + ", isp=null");
		}
		return isp;
	}

	@Test
	public void test01() {
		KnownListIsp isp = getIsp("210.173.87.154");	// KDDI CORPORATION
		assertTrue("equals same", isp.equals(isp));
		assertFalse("equals null", isp.equals(null));
		System.out.println("isp.hashCode()=" + isp.hashCode());

		if (check) {
			assertTrue("equals same", knownlist.equals(knownlist));
			assertFalse("equals null", knownlist.equals(null));
			System.out.println("knownlist.hashCode()=" + knownlist.hashCode());
		}
		else {
			// Do nothing
			// whoisクラスには、equalsやhashCode()の実装がない
		}

		getIsp("203.118.54.210");	// Nippon Steel Southeast Asia Pte Ltd
	}
	// 親ISPの情報が存在する場合
	@Test
	public void test02() {
		// 親ISP： Japan Network Information Center、211.8.0.0 - 211.19.255.255
		// WhoisApnic: addr=211.9.34.69 => name=BIT-DRIVE, country=JP, net=[211.9.32.0/22] 
		KnownListIsp isp = getIsp("211.9.34.69");
		assertNotNull(isp);
		if (check) {
			//assertEquals("BIT-DRIVE", isp.getName());
			//assertEquals("Sony Network Communications Inc.", isp.getName());
			assertTrue("BIT-DRIVE".equals(isp.getName())
					|| "Sony Network Communications Inc.".equals(isp.getName())
					);
		}
	}
	// キャッシュ動作 -> 2つ目のアドレス検索時にWhoisクラスがcallされないことを確認する
	@Test
	public void test03() {
		KnownListIsp isp1 = getIsp("210.173.87.154");
		KnownListIsp isp2 = getIsp("210.173.87.156");
		assertNotNull(isp1);
		assertNotNull(isp2);
		assertEquals(isp1, isp2);
		if (check) {
			assertEquals(isp1.getName(), isp2.getName());
			assertEquals(isp1.getAddress(), isp2.getAddress());
		}
	}
	
	// ネットワークアドレスが異なる同じISPの名称が一致すること
	@Test
	public void test04() {
		// Digital Ocean
		KnownListIsp isp1 = getIsp("104.236.241.73");
		KnownListIsp isp2 = getIsp("104.131.183.115");
		KnownListIsp isp3 = getIsp("138.197.18.68");
		KnownListIsp isp4 = getIsp("45.55.0.0");
		assertNotNull(isp1);
		assertNotNull(isp2);
		assertNotNull(isp3);
		assertNotNull(isp4);
		if (check) {
			assertEquals(isp1.getName(), isp2.getName());
			assertEquals(isp1.getName(), isp3.getName());
			assertEquals(isp1.getName(), isp4.getName());
		}
	}
	// delete "(.*)" at the end of the line
	@Test
	public void test05() {
		Pattern p = Pattern.compile("\\(.*\\)$");
		Matcher m;

		KnownListIsp isp = getIsp("64.134.171.160");
		assertNotNull(isp);
		if (check) {
			m = p.matcher(isp.getName());
			assertFalse(m.find());
		}

		isp = getIsp("216.58.220.238");
		assertNotNull(isp);
		if (check) {
			m = p.matcher(isp.getName());
			assertFalse(m.find());
		}
	}
	// null address
	@Test
	public void test06() {
		KnownListIsp isp = getIsp("260.1.1.1");
		assertTrue(isp == null || isp.getName() == null || isp.getName().equals("260.1.1.1"));
	}
	// from knownfile
	@Test
	public void test07() {
		KnownListIsp isp = getIsp("93.144.39.100");
		assertNotNull(isp);
		if (check) {
			assertEquals("Vodafone DSL Italy", isp.getName());
		}

		isp = getIsp("151.0.141.74");
		assertNotNull(isp);
		if (check) {
			//assertEquals("Fastweb SpA", isp.getName());
			assertTrue("Fastweb SpA".equals(isp.getName())
					|| "Fastweb Networks block".equals(isp.getName())
					);
		}

		isp = getIsp("51.38.12.13");
		assertNotNull(isp);
		if (check) {
			assertEquals("OVH SAS", isp.getName());
		}
	}
	// 上位プロバイダ[ARTERIA Networks Corporation] が存在するアドレス
	@Test
	public void test08() {
		KnownListIsp isp = getIsp("113.33.234.133");
		assertNotNull(isp);
		if (check) {
			assertEquals("NS Solutions Corporation", isp.getName());
		}

		isp = getIsp("124.35.68.168");
		assertNotNull(isp);
		if (check) {
			assertEquals("MOL Information Systems, Ltd.", isp.getName());
		}
	}
	// 対象の属性名が複数存在している場合に、優先度に従い取得されていること
	@Test
	public void test09() {
		// Organization -> CustName hit
		KnownListIsp isp = getIsp("71.6.146.185");
		assertNotNull(isp);
		if (check) {
			assertEquals("CariNet, Inc.", isp.getName());
		}

		// replace descr in SearchMapDeserializer
		isp = getIsp("94.102.49.190");
		assertNotNull(isp);
		if (check) {
			assertEquals("Quasi Networks LTD.", isp.getName());
		}
	}
	// lacnic or ripe で定義されているISPの取得ができること
	@Test
	public void test10() {
		KnownListIsp isp = getIsp("201.6.0.0");
		if (check) {
			assertNotEquals(Constants.UNKNOWN_COUNTRY, isp.getCountry());
		}
		isp = getIsp("187.95.130.0");
		if (check) {
			assertNotEquals(Constants.UNKNOWN_COUNTRY, isp.getCountry());
		}

		// ripe
		isp = getIsp("139.162.106.181");
		if (check) {
			assertNotEquals(Constants.UNKNOWN_COUNTRY, isp.getCountry());
		}
	}

}
