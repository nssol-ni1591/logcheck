package logcheck.test;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import javax.enterprise.util.AnnotationLiteral;

import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import logcheck.*;
import logcheck.annotations.UseChecker14;
import logcheck.annotations.UseChecker23;
import logcheck.annotations.UseChecker50;
import logcheck.annotations.UseChecker8;
import logcheck.util.WeldWrapper;

public class CheckerMainTest {

	private static Weld weld;
	private static WeldContainer container;

	public static String SSLINDEX	= Env.SSLINDEX;
	public static String MAGLIST	= Env.MAGLIST;
	public static String KNOWNLIST	= Env.KNOWNLIST;
	public static String SDCLIST	= Env.SDCLIST;
	public static String ACCESSLOG	= Env.VPNLOG;
	public static String FWLOG		= Env.FWLOG;

	// @Ruleアノテーションと共に、ExpectedSystemExitクラスのインスタンス作成
	@Rule
	public final ExpectedSystemExit exit = ExpectedSystemExit.none();
	@Rule
	public final SystemOutRule  stdout = new SystemOutRule();	

	@BeforeClass
	public static void beforeClass() {
		// システムプロパティの設定は早めに行う
		Env.init();

		System.out.println("start CheckerMainTest ...");

		weld = new Weld();
		container = weld.initialize();
		//Env.init()
	}
	@AfterClass
	public static void afterClass() {
		if (container != null) {
			container.close();
		}
		System.out.println("CheckerMainTest ... end");
	}

	@Before
	public void before() {
		stdout.enableLog();
	}
	@After
	public void after() {
		// Do nothing
	}

	@Test
	public void test3() throws IOException {
		Checker3 application = container.select(Checker3.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test3 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 72, count);
		
		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker3.main(KNOWNLIST);
	}
	@Test
	public void test4() throws IOException {
		Checker4 application = container.select(Checker4.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test4 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 261, count);

		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker4.main(KNOWNLIST);
	}
	@Test
	public void test5() throws IOException {
		Checker5 application = container.select(Checker5.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test5 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 103, count);

		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker5.main(KNOWNLIST);
	}
	@Test
	public void test6() throws IOException {
		Checker6 application = container.select(Checker6.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test6 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 150, count);

		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker6.main(KNOWNLIST);
	}
	@Test
	public void test7() throws IOException {
		Checker7 application = container.select(Checker7.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test7 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 61, count);

		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker7.main(KNOWNLIST);
	}
	@Test
	public void test8() throws IOException {
		Checker8 application = container.select(Checker8.class, new AnnotationLiteral<UseChecker8>(){
			private static final long serialVersionUID = 1L;
		}).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test8 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 88, count);

		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker8.main(KNOWNLIST);
	}
	@Test
	public void test9() throws IOException {
		Checker9 application = container.select(Checker9.class).get();
		int rc = new WeldWrapper().exec(application, 3, "2017-01-30", KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test9 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 184, count);

		rc = new WeldWrapper().exec(application, 3, "2017-01-30", KNOWNLIST);
		assertEquals("CheckerMainTest#test9 ... NG", 2, rc);
		rc = new WeldWrapper().exec(application, 3, "2017-01-", KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test9 ... NG", 3, rc);

		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker9.main("2017-01-01", KNOWNLIST);
	}

	@Test
	public void test10() throws IOException {
		Checker10 application = container.select(Checker10.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test10 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 10, count);

		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker10.main(KNOWNLIST);
	}

	@Test
	public void test12() throws IOException {
		Checker12 application = container.select(Checker12.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test12 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 4, count);

		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker12.main(KNOWNLIST);
	}
	@Test
	public void test13() throws IOException {
		Checker13 application = container.select(Checker13.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test13 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 5, count);

		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker13.main(KNOWNLIST);
	}
	@Test
	public void test14() throws IOException {
		stdout.mute();
		Checker14 application = container.select(Checker14.class, new AnnotationLiteral<UseChecker14>(){
			private static final long serialVersionUID = 1L;
		}).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, SSLINDEX, ACCESSLOG);
		assertEquals("CheckerMainTest#test10 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 3514, count);
		stdout.clearLog();

		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker14.main(KNOWNLIST);
	}
	@Test
	public void test15() throws IOException {
		Checker15 application = container.select(Checker15.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, SSLINDEX, ACCESSLOG);
		assertEquals("CheckerMainTest#test15 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 148, count);

		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker15.main(KNOWNLIST);
	}
	@Test
	public void test16() throws IOException {
		Checker16 application = container.select(Checker16.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, SSLINDEX, ACCESSLOG);
		assertEquals("CheckerMainTest#test16 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 1628, count);

		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker16.main(KNOWNLIST);
	}
	@Test
	public void test17() throws IOException {
		Checker17 application = container.select(Checker17.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, SSLINDEX, ACCESSLOG);
		assertEquals("CheckerMainTest#test17 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 3008, count);

		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker17.main(KNOWNLIST);
	}
	@Test
	public void test18() throws IOException {
		Checker18 application = container.select(Checker18.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, SSLINDEX, ACCESSLOG);
		assertEquals("CheckerMainTest#test18 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 3514, count);

		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker18.main(KNOWNLIST);
	}
	@Test
	public void test19() throws IOException {
		Checker19 application = container.select(Checker19.class).get();
		int rc = new WeldWrapper().exec(application, 2, null, null, ACCESSLOG);
		assertEquals("CheckerMainTest#test19 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 1024, count);

		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker19.main(KNOWNLIST);
	}
	@Test
	public void test21() throws IOException {
		Checker21 application = container.select(Checker21.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test21 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 170, count);

		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker21.main(KNOWNLIST);
	}
	@Test
	public void test22() throws IOException {
		Checker22 application = container.select(Checker22.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test22 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 170, count);

		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker22.main(KNOWNLIST);
	}
	@Test
	public void test23() throws IOException {
		Checker23 application = container.select(Checker23.class, new AnnotationLiteral<UseChecker23>(){
			private static final long serialVersionUID = 1L;
		}).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test23 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 30, count);

		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker23.main(KNOWNLIST);
	}
	@Test
	public void test25() throws IOException {
		Checker25 application = container.select(Checker25.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test25 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 43, count);

		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker25.main(KNOWNLIST);
	}
	@Test
	public void test50() throws IOException {
		stdout.mute();
		Checker50 application = container.select(Checker50.class, new AnnotationLiteral<UseChecker50>(){
			private static final long serialVersionUID = 1L;
		}).get();
		int rc = new WeldWrapper().exec(application, 3, KNOWNLIST, MAGLIST, SDCLIST, FWLOG);
		assertEquals("CheckerMainTest#test50 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 2813, count);

		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker50.main(KNOWNLIST);
	}
	@Test
	public void test51() throws IOException {
		Checker51 application = container.select(Checker51.class).get();
		int rc = new WeldWrapper().exec(application, 3, KNOWNLIST, MAGLIST, SDCLIST, FWLOG);
		assertEquals("CheckerMainTest#test51 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 2813, count);

		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker51.main(KNOWNLIST);
	}
	@Test
	public void test52() throws IOException {
		Checker52 application = container.select(Checker52.class).get();
		int rc = new WeldWrapper().exec(application, 3, KNOWNLIST, MAGLIST, SDCLIST, FWLOG);
		assertEquals("CheckerMainTest#test52 ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 2813, count);

		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker52.main(KNOWNLIST);
	}

}
