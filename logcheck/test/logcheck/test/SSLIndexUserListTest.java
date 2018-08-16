package logcheck.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.user.impl.SSLIndexBean;
import logcheck.user.impl.SSLIndexUserList;


public class SSLIndexUserListTest {

	private static SSLIndexUserList map;

	@BeforeClass
	public static void beforeClass() throws IOException {
		System.out.println("start SSLIndexUserListTest ...");

		map = new SSLIndexUserList();
		map = map.load(Env.SSLINDEX, null);
		System.out.println("size=" + map.size());
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("SSLIndexUserListTest ... end");
	}
	@Before
	public void before() throws IOException {
	}

	@Test
	public void test01() {
		assertFalse(map.isEmpty());
	}
	@Test(expected = IOException.class)
	public void test02() throws IOException {
		SSLIndexUserList map = new SSLIndexUserList();
		map = map.load("none", null);
	}
	@Test
	public void test03() {
		String s = "R	280523024633Z	180526025247Z	C6ED0D7516E9DB64	unknown	/C=JP/ST=TOKYO/L=CHUOU-KU/O=sdc/OU=nssol/CN=Z12756";
		SSLIndexBean bean = SSLIndexBean.parse(s);
		assertEquals("getFlag: ", "0", bean.getFlag());
		assertEquals("getExpire: ", "280523024633Z", bean.getExpire());
		assertEquals("getRevoce: ", "180526025247Z", bean.getRevoce());
		assertEquals("getSerial: ", "C6ED0D7516E9DB64", bean.getSerial());
		assertEquals("getFilename: ", "unknown", bean.getFilename());
		assertEquals("getUserId: ", "Z12756", bean.getUserId());
	}
}
