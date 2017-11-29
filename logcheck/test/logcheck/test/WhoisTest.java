package logcheck.test;

import static org.junit.Assert.assertNotNull;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.known.KnownListIsp;
import logcheck.known.net.Whois;
import logcheck.util.net.ClientAddr;


public class WhoisTest {

	private static WeldContainer container;
	private static Weld weld;

	private static Whois f;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setupWhoisTest() throws Exception {
		System.out.println("start WhoisTest ...");

		System.setProperty("proxySet" , "true");
		System.setProperty("proxyHost", "proxy.ns-sol.co.jp");
		System.setProperty("proxyPort", "8000");

		weld = new Weld();
		container = weld.initialize();
		f = container.select(Whois.class).get();
	}
	@AfterClass
	public static void afterClass() throws Exception {
		container.close();
		System.out.println("WhoisTest ... end");
	}

	private KnownListIsp getIsp(String addr) {
		KnownListIsp isp = f.get(new ClientAddr(addr));
		assertNotNull(isp);
		System.out.println("addr=" + addr + ", isp=[" + isp + ", C=" + isp.getCountry() +", NET=" + isp.getAddress() + "]");
		return isp;
	}

	@Test
	public void test01() {
		assertNotNull(getIsp("210.1.29.82"));
	}
	@Test
	public void test02() {
		assertNotNull(getIsp("210.173.87.154"));
	}
	@Test
	public void test03() {
		assertNotNull(getIsp("110.77.214.76"));
	}
	@Test
	public void test04() {
		assertNotNull(getIsp("70.62.31.2"));
	}
	@Test
	public void test05() {
		assertNotNull(getIsp("64.134.171.160"));
	}

	

//				new ClientAddr(""),
//				new ClientAddr(""),
//				new ClientAddr(""),
/*
				new ClientAddr("119.72.196.172"),
				new ClientAddr("124.35.68.170"),
				new ClientAddr("119.224.170.26"),
				new ClientAddr("62.173.40.229"),
				new ClientAddr("61.204.36.71"),
				new ClientAddr("202.248.61.202"),
				new ClientAddr("61.204.36.81"),
				new ClientAddr("202.248.61.202"),
				new ClientAddr("61.204.36.71"),
*/

//				new ClientAddr("210.1.29.82"),
/*
				new ClientAddr("182.232.195.22"),
				new ClientAddr("203.87.156.92"),
				new ClientAddr("182.48.105.210"),
				new ClientAddr("60.251.66.155"),
				new ClientAddr("52.90.33.223"),
				new ClientAddr("106.140.52.162"),
				new ClientAddr("210.173.87.154"),

				new ClientAddr("70.62.31.2"),
				new ClientAddr("64.134.171.160"),
				new ClientAddr("110.77.214.76"),
				new ClientAddr("101.99.14.161"),
				new ClientAddr("59.153.233.226"),
				new ClientAddr("117.4.252.36"),
				new ClientAddr("222.252.17.6"),
				new ClientAddr("122.2.36.229"),
				new ClientAddr("93.150.63.11"),
				new ClientAddr("183.82.120.86"),
				new ClientAddr("103.40.133.2"),
				new ClientAddr("79.191.82.167"),
				new ClientAddr("219.90.84.2"),
				new ClientAddr("122.2.36.229"),
*/

}
