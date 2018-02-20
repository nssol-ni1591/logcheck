package logcheck.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.user.sslindex.SSLIndexUserList;


public class SSLIndexUserListTest {

	@BeforeClass
	public static void beforeClass() {
		System.out.println("start SSLIndexUserListTest ...");
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("SSLIndexUserListTest ... end");
	}

	@Test
	public void test01() throws IOException {
		SSLIndexUserList map = new SSLIndexUserList();
		map = map.load(Env.SSLINDEX, null);
		System.out.println("size=" + map.size());
		assertFalse(map.isEmpty());
	}

	@Test(expected = IOException.class)
	public void test02() throws IOException {
		SSLIndexUserList map = new SSLIndexUserList();
		map = map.load("none", null);
	}
}
