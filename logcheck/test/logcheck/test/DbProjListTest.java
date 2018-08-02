package logcheck.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.proj.ProjListBean;
import logcheck.proj.db.DbProjList;


/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
public class DbProjListTest {

	private static DbProjList map;

	@BeforeClass
	public static void beforeClass() throws ClassNotFoundException, IOException, SQLException {
		System.out.println("start DbProjListTest ...");

		map = new DbProjList();
		map.init();
		map.load();
		System.out.println("size=" + map.size());
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("DbProjListTest ... end");
	}
	@Before
	public void before() throws ClassNotFoundException, IOException, SQLException {
	}

	@Test
	public void test01() {
		assertFalse(map.isEmpty());
		// select count(*) from mst_project;
		assertEquals(1010, map.size());
		assertFalse(map.equals(null));
		System.out.println("hashCode()=" + map.hashCode());
	}
	@Test
	public void test02() {
		int ix = 0;
		for (ProjListBean bean : map.values()) {
			assertNotNull("getProjIs() is null", bean.getProjId());
			assertNotNull("getValidFlag() is null", bean.getValidFlag());
			ix += 1;
		}
		System.out.println("count: " + ix);

		assertTrue("PRJ_SDC not found", map.containsKey("PRJ_SDC"));
		assertTrue("PRJ_SDC_OM not found", map.containsKey("PRJ_SDC_OM"));
	}
	@Test
	public void test03() {
		ProjListBean p1 = new ProjListBean("PRJ_1234", "V");
		ProjListBean p2 = new ProjListBean("PRJ_2234", "V");
		ProjListBean p3 = new ProjListBean("PRJ_3234", "V");
		ProjListBean p4 = new ProjListBean("PRJ_1234", "V");

		assertTrue("equals true", p1.equals(p1));
		assertTrue("equals true", p1.equals(p4));
		assertFalse("equals false", p1.equals(p2));
		//assertFalse("equals null", p1.equals(null));

		assertTrue("compareTo == 0", p2.compareTo(p2) == 0);
		assertTrue("compareTo < 0", p2.compareTo(p3) < 0);
		assertTrue("compareTo > 0", p2.compareTo(p1) > 0);

		System.out.println("p1: " + p1);
		System.out.println("hashCode()=" + p1.hashCode());
	}
}
