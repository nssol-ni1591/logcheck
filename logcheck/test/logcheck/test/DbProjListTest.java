package logcheck.test;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.proj.ProjListBean;
import logcheck.proj.db.DbProjList;


/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
public class DbProjListTest {

	@BeforeClass
	public static void beforeClass() {
		System.out.println("start DbProjListTest ...");
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("DbProjListTest ... end");
	}

	@Test
	public void test() throws Exception {
		DbProjList map = new DbProjList();
		map.load();
		System.out.println("size=" + map.size());
		assertFalse(map.isEmpty());

		int ix = 0;
		for (ProjListBean bean : map.values()) {
			assertNotNull("getProjIs() is null", bean.getProjId());
			assertNotNull("getValidFlag() is null", bean.getValidFlag());
			assertFalse("equlas(null)", bean.equals(null));
			ix += 1;
		}
		System.out.println("count: " + ix);
	}

}
