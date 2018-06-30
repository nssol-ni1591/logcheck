package logcheck.test;

import java.util.Map;

import logcheck.log.AccessLog;
import logcheck.log.AccessLogSummary;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AccessLogTest {

	private static WeldContainer container;
	private static Weld weld;

	private AccessLog test;
	private Map<String, AccessLogSummary> map;

	@BeforeClass
	public static void beforeClass() {
		System.out.println("start AccessLogTest ...");

		weld = new Weld();
		container = weld.initialize();
	}
	@AfterClass
	public static void afterClass() {
		container.close();

		System.out.println("AccessLogTest ... end");
	}

	@Before
	public void before() {
		test = container.select(AccessLog.class).get();
		map = test.load(Env.VPNLOG);
		System.out.println("size=" + map.size());
	}
	@After
	public void after() {
	}

	@Test
	public void test01() {
		assumeTrue(map.size() > 0);
	}
	@Test
	public void test02() {
		AccessLogSummary sum = null;
		for (AccessLogSummary s : map.values()) {
			sum = s;
		}
		assertFalse(sum == null);
		System.out.println("AccessLogSummary: " + sum);
		
		assertNotNull("getAddr() is null", sum.getAddr());
		assertNotNull("getAfterUsrId() is null", sum.getAfterUsrId());
		assertNotNull("getCount() is null", sum.getCount());
		assertNotNull("getDetail() is null", sum.getDetail());
		assertNotNull("getFirstDate() is null", sum.getFirstDate());
		assertNotNull("getId() is null", sum.getId());
		assertNull("getIsp() is null", sum.getIsp());
		assertNotNull("getLastDate() is null", sum.getLastDate());
		assertNull("getPattern() is null", sum.getPattern());
		assertNotNull("getReason() is null", sum.getReason());
		assertNotNull("getRoles() is null", sum.getRoles());
		
		assertTrue("equals same object", sum.equals(sum));
		assertFalse("equals null", sum.equals(null));

		System.out.println("hashCode()=" + sum.hashCode());
	}
	@Test
	public void test03() {
		Map<String, AccessLogSummary> map = test.load("abc.log");
		assumeTrue("log's map is empty", map.isEmpty());
	}
}
