package logcheck.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.known.KnownList;
import logcheck.known.KnownListIsp;
import logcheck.known.tsv.TsvKnownList;

/*
 * 既知のISPのIPアドレスを取得する
 * 取得先は、引数に指定された「既知ISP_IPアドレス一覧」ファイル
 * 
 * 問題点：
 * 広いアドレス空間をISPが取得し、その一部を企業に貸し出しているよう場合、
 * IPアドレスから取得される接続元はISP名ではなく企業名を取得したい。
 * 今のHashMapでは、Hash地の値により、どちらが取得されるか判断付かない。
 */
public class TsvKnownListTest {

	@BeforeClass
	public static void beforeClass() {
		System.out.println("start TsvKnownListTest ...");
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("TsvKnownListTest ... end");
	}

	@Before
	public void before() {
	}
	@After
	public void after() {
	}

	@Test
	public void test01() throws IOException {
		// TsvKnownList はAlternativeによりWeld環境では使用できない?
		KnownList map = new TsvKnownList();
		map.init();
		map.load(Env.KNOWNLIST);
		System.out.println("TsvKnownListTest.test01: size=" + map.size());
		assertFalse(map.isEmpty());

		for (KnownListIsp n : map) {
			System.out.println(n.getCountry() + "\t" + n + "\t" + n.getAddress());
			System.out.print("\t");
			n.getAddress().forEach(s -> System.out.printf("[%s]", s.toStringRange()));
			System.out.println();
		}
	}

	@Test(expected = NoSuchFileException.class)
	public void test02() throws IOException {
		KnownList map = new TsvKnownList();
		map.init();
		map.load("abc");
	}

}
