package logcheck.test;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.user.UserListBean;
import logcheck.user.UserListSite;
import logcheck.user.db.DbUserList;

/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
public class DbUserListTest {

	@BeforeClass
	public static void beforeClass() {
		System.out.println("start DbUserListTest ...");
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("DbUserListTest ... end");
	}

	@Test
	public void test01() throws Exception {
		DbUserList map = new DbUserList();
		map.load(null, null);
		System.out.println("size=" + map.size());
		assertFalse(map.isEmpty());
		
		int ix = 0;
		int iy = 0;
		for (String userId : map.keySet()) {
			UserListBean b = map.get(userId);
			for (UserListSite sum : b.getSites()) {
//				System.out.println("userId=" + userId + " (" + b.getValidFlag() + "), sum=[" + sum + "]");
				sum.getAddress();
				sum.getCount();
				sum.getCountry();
				sum.getFirstDate();
				sum.getLastDate();
				sum.getProjDelFlag();
				sum.getProjId();
				sum.getSiteDelFlag();
				sum.getSiteId();
				sum.getSiteName();
				sum.getUserDelFlag();
				ix += 1;
			}
			iy += 1;
		}
		System.out.println("user.count: " + iy + ", gip.count: " + ix);
	}

}
