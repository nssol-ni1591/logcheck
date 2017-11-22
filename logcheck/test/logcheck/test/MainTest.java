package logcheck.test;

//import static org.junit.Assert.*;

import javax.enterprise.util.AnnotationLiteral;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.*;
import logcheck.annotations.UseChecker14;
import logcheck.annotations.UseChecker8;

public class MainTest {

	@BeforeClass
	public static void beforeClass() {
		System.out.println("start MainTest ...");
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("MainTest ... end");
	}

	private static String MAGLIST   = "C:\\Users\\NI1591\\Desktop\\2017-セキュリティ対策\\xls\\maglist.txt";
	private static String KNOWNLIST = "C:\\Users\\NI1591\\Desktop\\2017-セキュリティ対策\\xls\\knownlist.txt";
	private static String ACCESSLOG = "C:\\Users\\NI1591\\Desktop\\2017-セキュリティ対策\\VPN-LOG\\access.log";
	private static String SSLINDEX = "C:\\Users\\NI1591\\Desktop\\2017-セキュリティ対策\\xls\\index.txt";

	@Test
	public void test3() {
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker3 application = container.select(Checker3.class).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
		}
	}
	@Test
	public void test4() {
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker4 application = container.select(Checker4.class).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
		}
	}
	@Test
	public void test5() {
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker5 application = container.select(Checker5.class).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
		}
	}
	@Test
	public void test6() {
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker6 application = container.select(Checker6.class).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
		}
	}
	@Test
	public void test7() {
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker7 application = container.select(Checker7.class).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
//			ex.printStackTrace(System.err);
		}
	}
	@Test
	public void test8() {
//		logcheck.Checker8.main(KNOWNLIST, MAGLIST, ACCESSLOG);
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
//			Checker8 application = container.instance().select(Checker8.class, new AnnotationLiteral<UseChecker8>(){
			Checker8 application = container.select(Checker8.class, new AnnotationLiteral<UseChecker8>(){
				private static final long serialVersionUID = 1L;
			}).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
//			ex.printStackTrace(System.err);
		}
	}
	@Test
	public void test9() {
//		logcheck.Checker9.main("2017-01-30", KNOWNLIST, MAGLIST, ACCESSLOG);
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
//			Checker9 application = container.instance().select(Checker9.class).get();
			Checker9 application = container.select(Checker9.class).get();
			application.init(KNOWNLIST, MAGLIST, "2017-01-30").start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
//			ex.printStackTrace(System.err);
		}
	}

	@Test
	public void test10() {
//		logcheck.Checker10.main(KNOWNLIST, MAGLIST, ACCESSLOG);
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
//			Checker10 application = container.instance().select(Checker10.class).get();
			Checker10 application = container.select(Checker10.class).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
//			ex.printStackTrace(System.err);
		}
	}

	@Test
	public void test12() {
//		logcheck.Checker12.main(KNOWNLIST, MAGLIST, ACCESSLOG);
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
//			Checker12 application = container.instance().select(Checker12.class).get();
			Checker12 application = container.select(Checker12.class).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
//			ex.printStackTrace(System.err);
		}
	}
	@Test
	public void test13() {
//		logcheck.Checker13.main(KNOWNLIST, MAGLIST, ACCESSLOG);
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
//			Checker13 application = container.instance().select(Checker13.class).get();
			Checker13 application = container.select(Checker13.class).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
//			ex.printStackTrace(System.err);
		}
	}
	@Test
	public void test14() {
//		logcheck.Checker14.main(KNOWNLIST, MAGLIST, ACCESSLOG);
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
//			Checker14 application = container.instance().select(Checker14.class, new AnnotationLiteral<UseChecker14>(){
			Checker14 application = container.select(Checker14.class, new AnnotationLiteral<UseChecker14>(){
				private static final long serialVersionUID = 1L;
			}).get();
			application.init(KNOWNLIST, SSLINDEX).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
//			ex.printStackTrace(System.err);
		}
	}
	@Test
	public void test15() {
//		logcheck.Checker15.main(KNOWNLIST, MAGLIST, ACCESSLOG);
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
//			Checker15 application = container.instance().select(Checker15.class).get();
			Checker15 application = container.select(Checker15.class).get();
			application.init(KNOWNLIST, SSLINDEX).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
//			ex.printStackTrace(System.err);
		}
	}
	@Test
	public void test16() {
//		logcheck.Checker16.main(KNOWNLIST, MAGLIST, ACCESSLOG);
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
//			Checker16 application = container.instance().select(Checker16.class).get();
			Checker16 application = container.select(Checker16.class).get();
			application.init(KNOWNLIST, SSLINDEX).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
//			ex.printStackTrace(System.err);
		}
	}
	@Test
	public void test17() {
//		logcheck.Checker17.main(KNOWNLIST, MAGLIST, ACCESSLOG);
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
//			Checker17 application = container.instance().select(Checker17.class).get();
			Checker17 application = container.select(Checker17.class).get();
			application.init(KNOWNLIST, SSLINDEX).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
//			ex.printStackTrace(System.err);
		}
	}
	@Test
	public void test18() {
//		logcheck.Checker18.main(KNOWNLIST, MAGLIST, ACCESSLOG);
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
//			Checker18 application = container.instance().select(Checker18.class).get();
			Checker18 application = container.select(Checker18.class).get();
			application.init(KNOWNLIST, SSLINDEX).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
//			ex.printStackTrace(System.err);
		}
	}
	@Test
	public void test22() {
//		logcheck.Checker22.main(KNOWNLIST, MAGLIST, ACCESSLOG);
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
//			Checker22 application = container.instance().select(Checker22.class).get();
			Checker22 application = container.select(Checker22.class).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
//			ex.printStackTrace(System.err);
		}
	}
	@Test
	public void test23() {
//		logcheck.Checker23.main(KNOWNLIST, MAGLIST, ACCESSLOG);
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
//			Checker23 application = container.instance().select(Checker23.class).get();
			Checker23 application = container.select(Checker23.class).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
//			ex.printStackTrace(System.err);
		}
	}
	@Test
	public void test24() {
//		logcheck.Checker23.main(KNOWNLIST, MAGLIST, ACCESSLOG);
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
//			Checker23 application = container.instance().select(Checker23.class).get();
			Checker24 application = container.select(Checker24.class).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
//			ex.printStackTrace(System.err);
		}
	}
	@Test
	public void test25() {
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker24 application = container.select(Checker25.class).get();
			application.init(KNOWNLIST, MAGLIST).start(new String[] { ACCESSLOG }, 0);
		}
		catch (Exception ex) {
		}
	}
//	@Test
//	public void test50() {
//		logcheck.Checker50.main(KNOWNLIST, MAGLIST, ACCESSLOG);
//	}

}
