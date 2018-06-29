package logcheck.test;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.stream.Stream;

import javax.enterprise.util.AnnotationLiteral;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.AbstractChecker;
import logcheck.Checker12;
import logcheck.Checker14;
import logcheck.annotations.UseChecker14;
import logcheck.util.weld.WeldWrapper;

import static logcheck.test.CheckerMainTest.ACCESSLOG;
import static logcheck.test.CheckerMainTest.KNOWNLIST;
import static logcheck.test.CheckerMainTest.MAGLIST;
import static logcheck.test.CheckerMainTest.SSLINDEX;

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

	@Test
	public void test01() {
		// annoなし
		int rc = new WeldWrapper<Checker12>(Checker12.class)
				.weld(2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("Annoなし", 0, rc);
	}
	@Test
	public void test02() {
		// annoつき
		int rc = new WeldWrapper<Checker14>(Checker14.class)
				.weld(new AnnotationLiteral<UseChecker14>(){
					private static final long serialVersionUID = 1L;
				}, 2, KNOWNLIST, SSLINDEX, ACCESSLOG);
		assertEquals("Anno付", 0, rc);
	}
	@Test
	public void test03() {
		// 引数不足
		int rc = new WeldWrapper<Checker12>(Checker12.class)
				.weld(3, KNOWNLIST, MAGLIST);
		assertEquals("引数不足", 2, rc);
	}
	@Test
	public void test04() {
		// argcの値が不正
		int rc = new WeldWrapper<Checker12>(Checker12.class)
				.weld(4, KNOWNLIST, MAGLIST, ACCESSLOG, ACCESSLOG);
		assertEquals("argcの値が不正", 4, rc);
	}
	@Test
	public void test05() {
		// Weldで例外が発生する
		int rc = new WeldWrapper<WeldWrapperTestSub>(WeldWrapperTestSub.class)
				.weld(2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("Weld内で例外が発生した場合はcatchされて返却値が-1", -1, rc);
	}
	@Test
	public void test06() {
		// ファイル名が不正
		/* TsvKnownListで確認
		int rc = new WeldWrapper<Checker12>(Checker12.class)
				.weld(2, "abc.txt", MAGLIST, ACCESSLOG);
		assertEquals("KnownListが不正", 0, rc);
		*/
		/* DbSiteListが処理するため引数を参照していない
		int rc = new WeldWrapper<Checker12>(Checker12.class)
				.weld(2, KNOWNLIST, "abc.txt", ACCESSLOG);
		assertEquals("MagListが不正", 0, rc);
		*/
		int rc = new WeldWrapper<Checker12>(Checker12.class)
				.weld(2, KNOWNLIST, MAGLIST, "abc.txt");
		assertEquals("AccessLogが不正", 0, rc);
	}
	@Test
	public void test07() {
		InputStream backup = System.in;
		try (InputStream is = new FileInputStream(Env.VPNLOG)) {
			System.setIn(is);
			int rc = new WeldWrapper<Checker12>(Checker12.class)
					.weld(2, KNOWNLIST, MAGLIST);
			assertEquals("System.in 動作", 0, rc);
		}
		catch (IOException e) {
			System.err.println(e.getMessage());
		}
		finally {
			System.setIn(backup);
		}
	}

	public class WeldWrapperTestSub extends AbstractChecker<String> {

		@Override
		public void init(String... argv) throws IOException, ClassNotFoundException, SQLException {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected String call(Stream<String> stream) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected void report(PrintWriter out, String map) {
			// TODO Auto-generated method stub
			
		}

	}

}
