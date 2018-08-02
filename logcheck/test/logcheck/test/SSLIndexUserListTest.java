package logcheck.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
}
