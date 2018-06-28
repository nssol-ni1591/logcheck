package logcheck.test;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.enterprise.util.AnnotationLiteral;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.*;
import logcheck.annotations.UseChecker14;
import logcheck.annotations.UseChecker23;
import logcheck.annotations.UseChecker8;
import logcheck.util.weld.WeldWrapper;

public class CheckerMainTest {

	private static Weld weld;
	private static WeldContainer container;

	@BeforeClass
	public static void beforeClass() {
		System.out.println("start CheckerMainTest ...");

		weld = new Weld();
		container = weld.initialize();
		Env.init();
	}
	@AfterClass
	public static void afterClass() {
		if (container != null) {
			container.close();
		}
		System.out.println("CheckerMainTest ... end");
	}

	public static String SSLINDEX	= Env.SSLINDEX;
	public static String MAGLIST	= Env.MAGLIST;
	public static String KNOWNLIST	= Env.KNOWNLIST;
	public static String SDCLIST	= Env.SDCLIST;
	public static String ACCESSLOG	= Env.VPNLOG;
	public static String FWLOG		= Env.FWLOG;

	@Test
	public void test3() throws IOException {
		try (CharArrayWriter sw = new CharArrayWriter();
				PrintWriter out = new PrintWriter(sw);
				) {
			Checker3 application = container.select(Checker3.class).get();
			int rc = new WeldWrapper<Checker3>().exec(out, application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
			assertEquals("CheckerMainTest#test3 ... NG", 0, rc);

			BufferedReader br = new BufferedReader(new CharArrayReader(sw.toCharArray()));
			long count = br.lines().count();
			br.close();
			System.out.println("count=" + count);
			assertEquals("The number output line", 277, count);
		}
	}
	@Test
	public void test4() throws IOException {
		try (CharArrayWriter sw = new CharArrayWriter();
				PrintWriter out = new PrintWriter(sw);
				) {
			Checker4 application = container.select(Checker4.class).get();
			int rc = new WeldWrapper<Checker4>().exec(out, application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
			assertEquals("CheckerMainTest#test4 ... NG", 0, rc);

			BufferedReader br = new BufferedReader(new CharArrayReader(sw.toCharArray()));
			long count = br.lines().count();
			br.close();
			System.out.println("count=" + count);
			assertEquals("The number output line", 256, count);
		}
	}
	@Test
	public void test5() throws IOException {
		try (CharArrayWriter sw = new CharArrayWriter();
				PrintWriter out = new PrintWriter(sw);
				) {
			Checker5 application = container.select(Checker5.class).get();
			int rc = new WeldWrapper<Checker5>().exec(out, application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
			assertEquals("CheckerMainTest#test5 ... NG", 0, rc);

			BufferedReader br = new BufferedReader(new CharArrayReader(sw.toCharArray()));
			long count = br.lines().count();
			br.close();
			System.out.println("count=" + count);
			assertEquals("The number output line", 101, count);
		}
	}
	@Test
	public void test6() throws IOException {
		Checker6 application = container.select(Checker6.class).get();
		try (CharArrayWriter sw = new CharArrayWriter();
				PrintWriter out = new PrintWriter(sw);
				) {
			int rc = new WeldWrapper<Checker6>().exec(out, application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
			assertEquals("CheckerMainTest#test6 ... NG", 0, rc);

			BufferedReader br = new BufferedReader(new CharArrayReader(sw.toCharArray()));
			long count = br.lines().count();
			br.close();
			System.out.println("count=" + count);
			assertEquals("The number output line", 148, count);
		}
	}
	@Test
	public void test7() throws IOException {
		try (CharArrayWriter sw = new CharArrayWriter();
				PrintWriter out = new PrintWriter(sw);
				) {
			Checker7 application = container.select(Checker7.class).get();
			int rc = new WeldWrapper<Checker7>().exec(out, application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
			assertEquals("CheckerMainTest#test7 ... NG", 0, rc);

			BufferedReader br = new BufferedReader(new CharArrayReader(sw.toCharArray()));
			long count = br.lines().count();
			br.close();
			System.out.println("count=" + count);
			assertEquals("The number output line", 54, count);
		}
	}
	@Test
	public void test8() throws IOException {
		try (CharArrayWriter sw = new CharArrayWriter();
				PrintWriter out = new PrintWriter(sw);
				) {
			Checker8 application = container.select(Checker8.class, new AnnotationLiteral<UseChecker8>(){
				private static final long serialVersionUID = 1L;
			}).get();
			int rc = new WeldWrapper<Checker8>().exec(out, application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
			assertEquals("CheckerMainTest#test8 ... NG", 0, rc);

			BufferedReader br = new BufferedReader(new CharArrayReader(sw.toCharArray()));
			long count = br.lines().count();
			br.close();
			System.out.println("count=" + count);
			assertEquals("The number output line", 37, count);
		}
	}
	@Test
	public void test9() throws IOException {
		try (CharArrayWriter sw = new CharArrayWriter();
				PrintWriter out = new PrintWriter(sw);
				) {
			Checker9 application = container.select(Checker9.class).get();
			int rc = new WeldWrapper<Checker9>().exec(out, application, 3, "2017-01-30", KNOWNLIST, MAGLIST, ACCESSLOG);
			assertEquals("CheckerMainTest#test9 ... NG", 0, rc);

			BufferedReader br = new BufferedReader(new CharArrayReader(sw.toCharArray()));
			long count = br.lines().count();
			br.close();
			System.out.println("count=" + count);
			assertEquals("The number output line", 184, count);
		}

		Checker9 application = container.select(Checker9.class).get();
		int rc = new WeldWrapper<Checker9>().exec(application, 3, "2017-01-30", KNOWNLIST);
		assertEquals("CheckerMainTest#test9 ... NG", 2, rc);
		rc = new WeldWrapper<Checker9>().exec(application, 3, "2017-01-", KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("CheckerMainTest#test9 ... NG", 3, rc);
	}
	@Test
	public void test10() throws IOException {
		try (CharArrayWriter sw = new CharArrayWriter();
				PrintWriter out = new PrintWriter(sw);
				) {
			Checker10 application = container.select(Checker10.class).get();
			int rc = new WeldWrapper<Checker10>().exec(out, application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
			assertEquals("CheckerMainTest#test10 ... NG", 0, rc);

			BufferedReader br = new BufferedReader(new CharArrayReader(sw.toCharArray()));
			long count = br.lines().count();
			br.close();
			System.out.println("count=" + count);
			assertEquals("The number output line", 10, count);
		}
	}

	@Test
	public void test12() throws IOException {
		try (CharArrayWriter sw = new CharArrayWriter();
				PrintWriter out = new PrintWriter(sw);
				) {
			Checker12 application = container.select(Checker12.class).get();
			int rc = new WeldWrapper<Checker12>().exec(out, application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
			assertEquals("CheckerMainTest#test12 ... NG", 0, rc);

			BufferedReader br = new BufferedReader(new CharArrayReader(sw.toCharArray()));
			long count = br.lines().count();
			br.close();
			System.out.println("count=" + count);
			assertEquals("The number output line", 4, count);
		}
	}
	@Test
	public void test13() throws IOException {
		try (CharArrayWriter sw = new CharArrayWriter();
				PrintWriter out = new PrintWriter(sw);
				) {
			Checker13 application = container.select(Checker13.class).get();
			int rc = new WeldWrapper<Checker13>().exec(out, application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
			assertEquals("CheckerMainTest#test13 ... NG", 0, rc);

			BufferedReader br = new BufferedReader(new CharArrayReader(sw.toCharArray()));
			long count = br.lines().count();
			br.close();
			System.out.println("count=" + count);
			assertEquals("The number output line", 5, count);
		}
	}
	@Test
	public void test14() throws IOException {
		try (CharArrayWriter sw = new CharArrayWriter();
				PrintWriter out = new PrintWriter(sw);
				) {
			Checker14 application = container.select(Checker14.class, new AnnotationLiteral<UseChecker14>(){
				private static final long serialVersionUID = 1L;
			}).get();
			int rc = new WeldWrapper<Checker14>().exec(out, application, 2, KNOWNLIST, SSLINDEX, ACCESSLOG);
			assertEquals("CheckerMainTest#test10 ... NG", 0, rc);

			BufferedReader br = new BufferedReader(new CharArrayReader(sw.toCharArray()));
			long count = br.lines().count();
			br.close();
			System.out.println("count=" + count);
			assertEquals("The number output line", 3009, count);
		}
	}
	@Test
	public void test15() throws IOException {
		try (CharArrayWriter sw = new CharArrayWriter();
				PrintWriter out = new PrintWriter(sw);
				) {
			Checker15 application = container.select(Checker15.class).get();
			int rc = new WeldWrapper<Checker15>().exec(out, application, 2, KNOWNLIST, SSLINDEX, ACCESSLOG);
			assertEquals("CheckerMainTest#test15 ... NG", 0, rc);

			BufferedReader br = new BufferedReader(new CharArrayReader(sw.toCharArray()));
			long count = br.lines().count();
			br.close();
			System.out.println("count=" + count);
			assertEquals("The number output line", 127, count);
		}
	}
	@Test
	public void test16() throws IOException {
		try (CharArrayWriter sw = new CharArrayWriter();
				PrintWriter out = new PrintWriter(sw);
				) {
			Checker16 application = container.select(Checker16.class).get();
			int rc = new WeldWrapper<Checker16>().exec(out, application, 2, KNOWNLIST, SSLINDEX, ACCESSLOG);
			assertEquals("CheckerMainTest#test16 ... NG", 0, rc);

			BufferedReader br = new BufferedReader(new CharArrayReader(sw.toCharArray()));
			long count = br.lines().count();
			br.close();
			System.out.println("count=" + count);
			assertEquals("The number output line", 1343, count);
		}
	}
	@Test
	public void test17() throws IOException {
		try (CharArrayWriter sw = new CharArrayWriter();
				PrintWriter out = new PrintWriter(sw);
				) {
			Checker17 application = container.select(Checker17.class).get();
			int rc = new WeldWrapper<Checker17>().exec(out, application, 2, KNOWNLIST, SSLINDEX, ACCESSLOG);
			assertEquals("CheckerMainTest#test17 ... NG", 0, rc);

			BufferedReader br = new BufferedReader(new CharArrayReader(sw.toCharArray()));
			long count = br.lines().count();
			br.close();
			System.out.println("count=" + count);
			assertEquals("The number output line", 3008, count);
		}
	}
	@Test
	public void test18() throws IOException {
		try (CharArrayWriter sw = new CharArrayWriter();
				PrintWriter out = new PrintWriter(sw);
				) {
			Checker18 application = container.select(Checker18.class).get();
			int rc = new WeldWrapper<Checker18>().exec(out, application, 2, KNOWNLIST, SSLINDEX, ACCESSLOG);
			assertEquals("CheckerMainTest#test18 ... NG", 0, rc);

			BufferedReader br = new BufferedReader(new CharArrayReader(sw.toCharArray()));
			long count = br.lines().count();
			br.close();
			System.out.println("count=" + count);
			assertEquals("The number output line", 3009, count);
		}
	}
	@Test
	public void test19() throws IOException {
		try (CharArrayWriter sw = new CharArrayWriter();
				PrintWriter out = new PrintWriter(sw);
				) {
			Checker19 application = container.select(Checker19.class).get();
			int rc = new WeldWrapper<Checker19>().exec(out, application, 2, null, null, ACCESSLOG);
			assertEquals("CheckerMainTest#test19 ... NG", 0, rc);

			BufferedReader br = new BufferedReader(new CharArrayReader(sw.toCharArray()));
			long count = br.lines().count();
			br.close();
			System.out.println("count=" + count);
			assertEquals("The number output line", 1022, count);
		}
	}
	@Test
	public void test21() throws IOException {
		try (CharArrayWriter sw = new CharArrayWriter();
				PrintWriter out = new PrintWriter(sw);
				) {
			Checker21 application = container.select(Checker21.class).get();
			int rc = new WeldWrapper<Checker21>().exec(out, application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
			assertEquals("CheckerMainTest#test21 ... NG", 0, rc);

			BufferedReader br = new BufferedReader(new CharArrayReader(sw.toCharArray()));
			long count = br.lines().count();
			br.close();
			System.out.println("count=" + count);
			assertEquals("The number output line", 66, count);
		}
	}
	@Test
	public void test22() throws IOException {
		try (CharArrayWriter sw = new CharArrayWriter();
				PrintWriter out = new PrintWriter(sw);
				) {
			Checker22 application = container.select(Checker22.class).get();
			int rc = new WeldWrapper<Checker22>().exec(out, application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
			assertEquals("CheckerMainTest#test22 ... NG", 0, rc);

			BufferedReader br = new BufferedReader(new CharArrayReader(sw.toCharArray()));
			long count = br.lines().count();
			br.close();
			System.out.println("count=" + count);
			assertEquals("The number output line", 66, count);
		}
	}
	@Test
	public void test23() throws IOException {
		try (CharArrayWriter sw = new CharArrayWriter();
				PrintWriter out = new PrintWriter(sw);
				) {
			Checker23 application = container.select(Checker23.class, new AnnotationLiteral<UseChecker23>(){
				private static final long serialVersionUID = 1L;
			}).get();
			int rc = new WeldWrapper<Checker23>().exec(out, application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
			assertEquals("CheckerMainTest#test23 ... NG", 0, rc);

			BufferedReader br = new BufferedReader(new CharArrayReader(sw.toCharArray()));
			long count = br.lines().count();
			br.close();
			System.out.println("count=" + count);
			assertEquals("The number output line", 30, count);
		}
	}
	@Test
	public void test25() throws IOException {
		try (CharArrayWriter sw = new CharArrayWriter();
				PrintWriter out = new PrintWriter(sw);
				) {
			Checker25 application = container.select(Checker25.class).get();
			int rc = new WeldWrapper<Checker25>().exec(out, application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
			assertEquals("CheckerMainTest#test25 ... NG", 0, rc);

			BufferedReader br = new BufferedReader(new CharArrayReader(sw.toCharArray()));
			long count = br.lines().count();
			br.close();
			System.out.println("count=" + count);
			assertEquals("The number output line", 43, count);
		}
	}
	@Test
	public void test50() throws IOException {
		try (CharArrayWriter sw = new CharArrayWriter();
				PrintWriter out = new PrintWriter(sw);
				) {
			Checker50 application = container.select(Checker50.class).get();
			int rc = new WeldWrapper<Checker50>().exec(out, application, 3, KNOWNLIST, MAGLIST, SDCLIST, FWLOG);
			assertEquals("CheckerMainTest#test50 ... NG", 0, rc);

			BufferedReader br = new BufferedReader(new CharArrayReader(sw.toCharArray()));
			long count = br.lines().count();
			br.close();
			System.out.println("count=" + count);
			assertEquals("The number output line", 2813, count);
		}
	}

}
