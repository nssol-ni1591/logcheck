package logcheck.test;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

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

import logcheck.Checker12a;
import logcheck.Checker13a;
import logcheck.Checker3a;
import logcheck.Checker3b;
import logcheck.Checker3c;
import logcheck.Checker3d;
import logcheck.Checker3e;
import logcheck.Checker8a;
import logcheck.util.weld.WeldWrapper;

public class CheckerMainTest2 {

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
		System.out.println("start CheckerMainTest2 ...");

		weld = new Weld();
		container = weld.initialize();
		Env.init();
	}
	@AfterClass
	public static void afterClass() {
		if (container != null) {
			container.close();
		}
		System.out.println("CheckerMainTest2 ... end");
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
	public void test3a() throws IOException {
		Checker3a application = container.select(Checker3a.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test3a ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 277, count);
		
		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker3a.main(KNOWNLIST);
	}
	@Test
	public void test3b() throws IOException {
		Checker3b application = container.select(Checker3b.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test3b ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 277, count);
		
		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker3b.main(KNOWNLIST);
	}
	@Test
	public void test3c() throws IOException {
		Checker3c application = container.select(Checker3c.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test3c ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 277, count);
		
		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker3c.main(KNOWNLIST);
	}
	@Test
	public void test3d() throws IOException {
		Checker3d application = container.select(Checker3d.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test3d ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 277, count);
		
		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker3d.main(KNOWNLIST);
	}
	@Test
	public void test3e() throws IOException {
		Checker3e application = container.select(Checker3e.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test3e ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 277, count);
		
		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker3e.main(KNOWNLIST);
	}


	@Test
	public void test8a() throws IOException {
		Checker8a application = container.select(Checker8a.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test8a ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 37, count);
		
		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker8a.main(KNOWNLIST);
	}
	@Test
	public void test12a() throws IOException {
		Checker12a application = container.select(Checker12a.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test12a ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 4, count);
		
		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker12a.main(KNOWNLIST);
	}
	@Test
	public void test13a() throws IOException {
		Checker13a application = container.select(Checker13a.class).get();
		int rc = new WeldWrapper().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test13a ... NG", 0, rc);

		BufferedReader br = new BufferedReader(new StringReader(stdout.getLog()));
		long count = br.lines().count();
		br.close();
		System.out.println("count=" + count);
		assertEquals("The number output line", 5, count);
		
		// main（）実行とusageメッセージ出力
		exit.expectSystemExitWithStatus(2);
		Checker13a.main(KNOWNLIST);
	}

}
