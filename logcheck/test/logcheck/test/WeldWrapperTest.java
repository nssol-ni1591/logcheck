package logcheck.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

import javax.enterprise.util.AnnotationLiteral;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.Checker12;
import logcheck.Checker14;
import logcheck.annotations.UseChecker14;
import logcheck.util.weld.WeldRunner;
import logcheck.util.weld.WeldWrapper;

import static logcheck.test.MainTest.ACCESSLOG;
import static logcheck.test.MainTest.KNOWNLIST;
import static logcheck.test.MainTest.MAGLIST;
import static logcheck.test.MainTest.SSLINDEX;

public class WeldWrapperTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void beforeClass() throws Exception {
		System.out.println("start WeldWrapperTest ...");
		Env.init();

	}
	@AfterClass
	public static void afterClass() throws Exception {
		System.out.println("WeldWrapperTest ... end");
	}

//	@Test
//	public void test00() {
//		int rc = new WeldWrapper<WeldWrapperTestSub>(WeldWrapperTestSub.class)
//					.weld(2, KNOWNLIST, MAGLIST, ACCESSLOG);
//		assertEquals("WeldWrapperTest#test00 ... NG", 0, rc);
//	}

	@Test
	public void test01() {
		int rc = new WeldWrapper<Checker12>(Checker12.class)
					.weld(2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("WeldWrapperTest#test01 ... NG", 0, rc);
	}
	@Test
	public void test02() {
		int rc = new WeldWrapper<Checker14>(Checker14.class)
					.weld(new AnnotationLiteral<UseChecker14>(){
			private static final long serialVersionUID = 1L;
		}, 2, KNOWNLIST, SSLINDEX, ACCESSLOG);
		assertEquals("WeldWrapperTest#test02 ... NG", 0, rc);
	}
	@Test
	public void test03() {
		// 引数が不足しているので、WeldWrapperで例外が発生する
		int rc = new WeldWrapper<WeldWrapperTestSub3>(WeldWrapperTestSub3.class)
					.weld(4, KNOWNLIST, MAGLIST, ACCESSLOG, ACCESSLOG);
		assertEquals("WeldWrapperTest#test03 ... NG", -1, rc);
	}

	public class WeldWrapperTestSub3 implements WeldRunner {

		@Override
		public void init(String... argv)
				throws IOException, ClassNotFoundException, SQLException {
			// Do nothing
		}
		@Override
		public int start(String[] argv, int argc)
				throws InterruptedException, ExecutionException, IOException {
			return 0;
		}
		
	}

}
