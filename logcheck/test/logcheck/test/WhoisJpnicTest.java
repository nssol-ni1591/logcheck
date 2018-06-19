package logcheck.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.known.net.jpnic.WhoisJpnic;

public class WhoisJpnicTest extends WhoisKnownListTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setupClass() throws Exception {
		System.out.println("start WhoisJpnic ...");

		whois = new WhoisJpnic();
		whois.init();
	}
	@AfterClass
	public static void afterClass() throws Exception {
		System.out.println("WhoisJpnic ... end");
	}

	@Test
	public void test01() {
//		super.test01();
	}
	@Test
	public void test02() {
//		super.test02();
	}
	@Test
	public void test03() {
//		super.test03();
	}
	@Test
	public void test04() {
//		super.test04();
	}
	@Test
	public void test05() {
//		super.test05();
	}
	@Test
	public void test06() {
//		super.test06();
	}
	@Test
	public void test07() {
//		super.test07();
	}
	@Test
	public void test08() {
//		super.test08();
	}
	@Test
	public void test09() {
//		super.test09();
	}
	@Test
	public void test10() {
//		super.test10();
	}

}
