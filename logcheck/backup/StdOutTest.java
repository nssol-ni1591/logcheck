package logcheck.test;

import java.util.logging.Logger;

import logcheck.util.log.StdErrLogger;
import logcheck.util.log.StdOutLogger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class StdOutTest {

	private Logger out;
	private Logger err;

	@BeforeClass
	public static void beforeClass() {
		System.out.println("start StdOutTest ...");
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("StdOutTest ... end");
	}

	@Before
	public void before() {
	}
	@After
	public void after() {
	}

	@Test
	public void test01() {
		out = StdOutLogger.getLogger();
		err = StdErrLogger.getLogger();

		out.info("stdout ...");
		err.info("stderr ...");
	}

}
