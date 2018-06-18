package logcheck.test;

import static org.junit.Assert.assertEquals;

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

public class MainTest {

	private static Weld weld;
	private static WeldContainer container;

	@BeforeClass
	public static void beforeClass() {
		System.out.println("start MainTest ...");

		System.setProperty("proxySet" , "true");
		System.setProperty("proxyHost", "proxy.ns-sol.co.jp");
		System.setProperty("proxyPort", "8000");

		weld = new Weld();
		container = weld.initialize();
	}
	@AfterClass
	public static void afterClass() {
		container.close();
		System.out.println("MainTest ... end");
	}
/*
	public static String SSLINDEX  = "C:\\Users\\NI1591\\Desktop\\2017-セキュリティ対策\\xls\\index.txt";
	public static String MAGLIST   = "C:\\Users\\NI1591\\Desktop\\2017-セキュリティ対策\\xls\\maglist.txt";
	public static String KNOWNLIST = "C:\\Users\\NI1591\\Desktop\\2017-セキュリティ対策\\xls\\knownlist.txt";
	public static String SDCLIST   = "C:\\Users\\NI1591\\Desktop\\2017-セキュリティ対策\\xls\\sdclist.txt";
	public static String ACCESSLOG = "C:\\Users\\NI1591\\Desktop\\2017-セキュリティ対策\\VPN-LOG\\access.log";
	public static String FWLOG		= "C:\\Users\\NI1591\\Desktop\\2017-セキュリティ対策\\fortigate-LOG\\fw.log";
*/
	public static String SSLINDEX	= Env.SSLINDEX;
	public static String MAGLIST	= Env.MAGLIST;
	public static String KNOWNLIST	= Env.KNOWNLIST;
	public static String SDCLIST	= Env.SDCLIST;
	public static String ACCESSLOG	= Env.VPNLOG;
	public static String FWLOG		= Env.FWLOG;

	@Test
	public void test3() {
		Checker3 application = container.select(Checker3.class).get();
		int rc = new WeldWrapper<Checker3>().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("MainTest#test3 ... NG", 0, rc);
		rc = new WeldWrapper<Checker3>().exec(application, 2, KNOWNLIST);
		assertEquals("MainTest#test3 ... NG", 2, rc);
	}
	@Test
	public void test4() {
		Checker4 application = container.select(Checker4.class).get();
		int rc = new WeldWrapper<Checker4>().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("MainTest#test4 ... NG", 0, rc);
		rc = new WeldWrapper<Checker4>().exec(application, 2, KNOWNLIST);
		assertEquals("MainTest#test4 ... NG", 2, rc);
	}
	@Test
	public void test5() {
		Checker5 application = container.select(Checker5.class).get();
		int rc = new WeldWrapper<Checker5>().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("MainTest#test5 ... NG", 0, rc);
		rc = new WeldWrapper<Checker5>().exec(application, 2, KNOWNLIST);
		assertEquals("MainTest#test5 ... NG", 2, rc);
	}
	@Test
	public void test6() {
		Checker6 application = container.select(Checker6.class).get();
		int rc = new WeldWrapper<Checker6>().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("MainTest#test6 ... NG", 0, rc);
		rc = new WeldWrapper<Checker6>().exec(application, 2, KNOWNLIST);
		assertEquals("MainTest#test6 ... NG", 2, rc);
	}
	@Test
	public void test7() {
		Checker7 application = container.select(Checker7.class).get();
		int rc = new WeldWrapper<Checker7>().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("MainTest#test7 ... NG", 0, rc);
		rc = new WeldWrapper<Checker7>().exec(application, 2, KNOWNLIST);
		assertEquals("MainTest#test7 ... NG", 2, rc);
	}
	@Test
	public void test8() {
		Checker8 application = container.select(Checker8.class, new AnnotationLiteral<UseChecker8>(){
			private static final long serialVersionUID = 1L;
		}).get();
		int rc = new WeldWrapper<Checker8>().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("MainTest#test8 ... NG", 0, rc);
		rc = new WeldWrapper<Checker8>().exec(application, 2, KNOWNLIST);
		assertEquals("MainTest#test8 ... NG", 2, rc);
	}
	@Test
	public void test9() {
		Checker9 application = container.select(Checker9.class).get();
		int rc = new WeldWrapper<Checker9>().exec(application, 3, "2017-01-30", KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("MainTest#test9 ... NG", 0, rc);
		rc = new WeldWrapper<Checker9>().exec(application, 3, "2017-01-30", KNOWNLIST);
		assertEquals("MainTest#test9 ... NG", 2, rc);
		rc = new WeldWrapper<Checker9>().exec(application, 3, "2017-01-", KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("MainTest#test9 ... NG", 3, rc);
	}
	@Test
	public void test10() {
		Checker10 application = container.select(Checker10.class).get();
		int rc = new WeldWrapper<Checker10>().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("MainTest#test10 ... NG", 0, rc);
		rc = new WeldWrapper<Checker10>().exec(application, 2, KNOWNLIST);
		assertEquals("MainTest#test10 ... NG", 2, rc);
	}

	@Test
	public void test12() {
		Checker12 application = container.select(Checker12.class).get();
		int rc = new WeldWrapper<Checker12>().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("MainTest#test12 ... NG", 0, rc);
		rc = new WeldWrapper<Checker12>().exec(application, 2, KNOWNLIST);
		assertEquals("MainTest#test12 ... NG", 2, rc);
	}
	@Test
	public void test13() {
		Checker13 application = container.select(Checker13.class).get();
		int rc = new WeldWrapper<Checker13>().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("MainTest#test13 ... NG", 0, rc);
		rc = new WeldWrapper<Checker13>().exec(application, 2, KNOWNLIST);
		assertEquals("MainTest#test13 ... NG", 2, rc);
	}
	@Test
	public void test14() {
		Checker14 application = container.select(Checker14.class, new AnnotationLiteral<UseChecker14>(){
			private static final long serialVersionUID = 1L;
		}).get();
		int rc = new WeldWrapper<Checker14>().exec(application, 2, KNOWNLIST, SSLINDEX, ACCESSLOG);
		assertEquals("MainTest#test10 ... NG", 0, rc);
		rc = new WeldWrapper<Checker14>().exec(application, 2, KNOWNLIST);
		assertEquals("MainTest#test14 ... NG", 2, rc);
	}
	@Test
	public void test15() {
		Checker15 application = container.select(Checker15.class).get();
		int rc = new WeldWrapper<Checker15>().exec(application, 2, KNOWNLIST, SSLINDEX, ACCESSLOG);
		assertEquals("MainTest#test15 ... NG", 0, rc);
		rc = new WeldWrapper<Checker15>().exec(application, 2, KNOWNLIST);
		assertEquals("MainTest#test15 ... NG", 2, rc);
	}
	@Test
	public void test16() {
		Checker16 application = container.select(Checker16.class).get();
		int rc = new WeldWrapper<Checker16>().exec(application, 2, KNOWNLIST, SSLINDEX, ACCESSLOG);
		assertEquals("MainTest#test16 ... NG", 0, rc);
		rc = new WeldWrapper<Checker16>().exec(application, 2, KNOWNLIST);
		assertEquals("MainTest#test16 ... NG", 2, rc);
	}
	@Test
	public void test17() {
		Checker17 application = container.select(Checker17.class).get();
		int rc = new WeldWrapper<Checker17>().exec(application, 2, KNOWNLIST, SSLINDEX, ACCESSLOG);
		assertEquals("MainTest#test17 ... NG", 0, rc);
		rc = new WeldWrapper<Checker17>().exec(application, 2, KNOWNLIST);
		assertEquals("MainTest#test17 ... NG", 2, rc);
	}
	@Test
	public void test18() {
		Checker18 application = container.select(Checker18.class).get();
		int rc = new WeldWrapper<Checker18>().exec(application, 2, KNOWNLIST, SSLINDEX, ACCESSLOG);
		assertEquals("MainTest#test18 ... NG", 0, rc);
		rc = new WeldWrapper<Checker18>().exec(application, 2, KNOWNLIST);
		assertEquals("MainTest#test18 ... NG", 2, rc);
	}
	@Test
	public void test19() {
		Checker19 application = container.select(Checker19.class).get();
		int rc = new WeldWrapper<Checker19>().exec(application, 2, null, null, ACCESSLOG);
		assertEquals("MainTest#test19 ... NG", 0, rc);
		rc = new WeldWrapper<Checker19>().exec(application, 2, KNOWNLIST);
		assertEquals("MainTest#test19 ... NG", 2, rc);
	}
	@Test
	public void test21() {
		Checker21 application = container.select(Checker21.class).get();
		int rc = new WeldWrapper<Checker21>().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("MainTest#test21 ... NG", 0, rc);
		rc = new WeldWrapper<Checker21>().exec(application, 2, KNOWNLIST);
		assertEquals("MainTest#test21 ... NG", 2, rc);
	}
	@Test
	public void test22() {
		Checker22 application = container.select(Checker22.class).get();
		int rc = new WeldWrapper<Checker22>().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("MainTest#test22 ... NG", 0, rc);
		rc = new WeldWrapper<Checker22>().exec(application, 2, KNOWNLIST);
		assertEquals("MainTest#test22 ... NG", 2, rc);
	}
	@Test
	public void test23() {
		Checker23 application = container.select(Checker23.class, new AnnotationLiteral<UseChecker23>(){
			private static final long serialVersionUID = 1L;
		}).get();
		int rc = new WeldWrapper<Checker23>().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("MainTest#test23 ... NG", 0, rc);
		rc = new WeldWrapper<Checker23>().exec(application, 2, KNOWNLIST);
		assertEquals("MainTest#test23 ... NG", 2, rc);
	}
	@Test
	public void test25() {
		Checker25 application = container.select(Checker25.class).get();
		int rc = new WeldWrapper<Checker25>().exec(application, 2, KNOWNLIST, MAGLIST, ACCESSLOG);
		assertEquals("MainTest#test25 ... NG", 0, rc);
		rc = new WeldWrapper<Checker25>().exec(application, 2, KNOWNLIST);
		assertEquals("MainTest#test25 ... NG", 2, rc);
	}
	@Test
	public void test50() {
		Checker50 application = container.select(Checker50.class).get();
		int rc = new WeldWrapper<Checker50>().exec(application, 3, KNOWNLIST, MAGLIST, SDCLIST, FWLOG);
		assertEquals("MainTest#test50 ... NG", 0, rc);
		rc = new WeldWrapper<Checker50>().exec(application, 3, KNOWNLIST);
		assertEquals("MainTest#test50 ... NG", 2, rc);
	}

}
