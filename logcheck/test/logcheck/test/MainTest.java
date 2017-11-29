package logcheck.test;

//import static org.junit.Assert.*;

import javax.enterprise.util.AnnotationLiteral;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.*;
import logcheck.annotations.UseChecker14;
import logcheck.annotations.UseChecker23;
import logcheck.annotations.UseChecker8;

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
//		if (container != null) {
			container.close();
//		}
//		if (weld != null) {
//			weld.shutdown();
//		}

		System.out.println("MainTest ... end");
	}

	@Before
	public void before() {
		/*
		if (weld == null) {
			weld = new Weld();
		}
		if (container == null) {
			container = weld.initialize();
		}
		*/
	}
	@After
	public void after() {
	}

	private static String SSLINDEX  = "C:\\Users\\NI1591\\Desktop\\2017-セキュリティ対策\\xls\\index.txt";
	private static String MAGLIST   = "C:\\Users\\NI1591\\Desktop\\2017-セキュリティ対策\\xls\\maglist.txt";
	private static String KNOWNLIST = "C:\\Users\\NI1591\\Desktop\\2017-セキュリティ対策\\xls\\knownlist.txt";
	private static String SDCLIST   = "C:\\Users\\NI1591\\Desktop\\2017-セキュリティ対策\\xls\\sdclist.txt";
	private static String ACCESSLOG = "C:\\Users\\NI1591\\Desktop\\2017-セキュリティ対策\\VPN-LOG\\access.log";
//	private static String FWLOG		= "C:\\Users\\NI1591\\Desktop\\2017-セキュリティ対策\\fortigate-LOG\\ForwardTrafficLog-disk-2017-04-21T14-38-26.394251.log";
	private static String FWLOG		= "C:\\Users\\NI1591\\Desktop\\2017-セキュリティ対策\\fortigate-LOG\\fw.log";

	@Test
	public void test3() {
//		Weld weld = new Weld();
//		try (WeldContainer container = weld.initialize()) {
		try {
			Checker3 application = container.select(Checker3.class).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	@Test
	public void test4() {
//		Weld weld = new Weld();
//		try (WeldContainer container = weld.initialize()) {
		try {
			Checker4 application = container.select(Checker4.class).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	@Test
	public void test5() {
//		Weld weld = new Weld();
//		try (WeldContainer container = weld.initialize()) {
		try {
			Checker5 application = container.select(Checker5.class).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	@Test
	public void test6() {
//		Weld weld = new Weld();
//		try (WeldContainer container = weld.initialize()) {
		try {
			Checker6 application = container.select(Checker6.class).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	@Test
	public void test7() {
//		Weld weld = new Weld();
//		try (WeldContainer container = weld.initialize()) {
		try {
			Checker7 application = container.select(Checker7.class).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	@Test
	public void test8() {
//		Weld weld = new Weld();
//		try (WeldContainer container = weld.initialize()) {
		try {
			Checker8 application = container.select(Checker8.class, new AnnotationLiteral<UseChecker8>(){
				private static final long serialVersionUID = 1L;
			}).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	@Test
	public void test9() {
//		Weld weld = new Weld();
//		try (WeldContainer container = weld.initialize()) {
		try {
			Checker9 application = container.select(Checker9.class).get();
			application.init("2017-01-30", KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Test
	public void test10() {
//		Weld weld = new Weld();
//		try (WeldContainer container = weld.initialize()) {
		try {
			Checker10 application = container.select(Checker10.class).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Test
	public void test12() {
//		Weld weld = new Weld();
//		try (WeldContainer container = weld.initialize()) {
		try {
			Checker12 application = container.select(Checker12.class).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	@Test
	public void test13() {
//		Weld weld = new Weld();
//		try (WeldContainer container = weld.initialize()) {
		try {
			Checker13 application = container.select(Checker13.class).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	@Test
	public void test14() {
//		Weld weld = new Weld();
//		try (WeldContainer container = weld.initialize()) {
		try {
			Checker14 application = container.select(Checker14.class, new AnnotationLiteral<UseChecker14>(){
				private static final long serialVersionUID = 1L;
			}).get();
			application.init(KNOWNLIST, SSLINDEX).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	@Test
	public void test15() {
//		Weld weld = new Weld();
//		try (WeldContainer container = weld.initialize()) {
		try {
			Checker15 application = container.select(Checker15.class).get();
			application.init(KNOWNLIST, SSLINDEX).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	@Test
	public void test16() {
//		Weld weld = new Weld();
//		try (WeldContainer container = weld.initialize()) {
		try {
			Checker16 application = container.select(Checker16.class).get();
			application.init(KNOWNLIST, SSLINDEX).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	@Test
	public void test17() {
//		Weld weld = new Weld();
//		try (WeldContainer container = weld.initialize()) {
		try {
			Checker17 application = container.select(Checker17.class).get();
			application.init(KNOWNLIST, SSLINDEX).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	@Test
	public void test18() {
//		Weld weld = new Weld();
//		try (WeldContainer container = weld.initialize()) {
		try {
			Checker18 application = container.select(Checker18.class).get();
			application.init(KNOWNLIST, SSLINDEX).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	@Test
	public void test22() {
//		Weld weld = new Weld();
//		try (WeldContainer container = weld.initialize()) {
		try {
			Checker22 application = container.select(Checker22.class).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	@Test
	public void test23() {
//		Weld weld = new Weld();
//		try (WeldContainer container = weld.initialize()) {
		try {
			Checker23 application = container.select(Checker23.class, new AnnotationLiteral<UseChecker23>(){
				private static final long serialVersionUID = 1L;
			}).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	@Test
	public void test25() {
//		Weld weld = new Weld();
//		try (WeldContainer container = weld.initialize()) {
		try {
			Checker25 application = container.select(Checker25.class).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	@Test
	public void test50() {
		try {
			Checker50 application = container.select(Checker50.class).get();
			application.init(KNOWNLIST, MAGLIST, SDCLIST).start(new String[] { FWLOG }, 0);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
