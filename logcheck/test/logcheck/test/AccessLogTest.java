package logcheck.test;

import java.util.Map;

import logcheck.log.AccessLog;
import logcheck.log.AccessLogSummary;

import static org.junit.Assert.assertNotNull;
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
	}
	@After
	public void after() {
	}

	@Test
	public void test01() {
		Map<String, AccessLogSummary> map = test.load(Env.VPNLOG);
		System.out.println("size=" + map.size());
		assumeTrue("log's count is 35", map.size() == 35);

		for (AccessLogSummary sum : map.values()) {
			assertNotNull("getAddr() is null", sum.getAddr());
			assertNotNull("getAfterUsrId() is null", sum.getAfterUsrId());
			assertNotNull("getCount() is null", sum.getCount());
			assertNotNull("getDetail() is null", sum.getDetail());
			assertNotNull("getFirstDate() is null", sum.getFirstDate());
			assertNotNull("getId() is null", sum.getId());
			sum.getIsp();
			assertNotNull("getLastDate() is null", sum.getLastDate());
			sum.getPattern();
			assertNotNull("getReason() is null", sum.getReason());
			assertNotNull("getRoles() is null", sum.getRoles());
		}
	}
}
