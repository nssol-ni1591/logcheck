package logcheck.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

import logcheck.fw.FwLog;
import logcheck.fw.FwLogBean;
import logcheck.fw.FwLogSummary;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FwLogTest {

	@BeforeClass
	public static void beforeClass() throws IOException {
		System.err.println("start FwLogTest ...");
	}
	@AfterClass
	public static void aforeClass() {
		System.err.println("FwLogTest ... end");
	}

	private Map<FwLogBean, FwLogSummary> load(String file) throws IOException {
		System.err.println("FwLogTest.load: loading file=" + file);

		Map<FwLogBean, FwLogSummary> map = new TreeMap<>();
		Files.lines(Paths.get(file), StandardCharsets.UTF_8)
			.filter(FwLog::test)
			.map(FwLog::parse)
			.forEach(b -> {
				FwLogSummary prevSummary = null;
				FwLogSummary summary = map.get(b);
				if (summary == null) {
					summary = new FwLogSummary(b);
					map.put(b, summary);
				} else {
					summary.update(b.getDate());
				}

				summary.getFirstDate();
				summary.getLastDate();
				summary.getSrcAddr();
				summary.getDstAddr();
				summary.getSrcIsp();
				summary.getDstIsp();
				summary.getDstPort();
				summary.compareTo(prevSummary);
				summary.toString();

				assertNotNull("bean is null", b.toString());
				assertFalse("bean#equals(null)", b.equals(null));
				prevSummary = summary;
		});
		return map;
	}

	@Test
	public void test01() throws IOException {
		Map<FwLogBean, FwLogSummary> map = null;
		map = load(Env.FWLOG);
		//map.values().forEach(summary -> System.out.println("log: " + summary));

		// 正常動作なので、ログのコレクションが返却されて正常
		assertFalse(map.isEmpty());
	}

	@Test(expected = NoSuchFileException.class)
	public void test02() throws IOException {
		//Map<FwLogBean, FwLogSummary> map = null;
		load("a.txt");
		// 存在しないファイルを指定しているので、例外発生が正常
	}

	@Test
	public void test03() {
		FwLogBean b1 = new FwLogBean("2018-04-12", "00:00:45", "notice", "172.30.90.69", "43692", "128.221.236.246", "443");
		FwLogBean b2 = new FwLogBean("2018-04-12", "00:00:45", "notice", "172.30.90.69", "43692", "128.221.236.246", "443");

		b1.getDate();
		b1.getSrcIp();
		b1.getDstIp();
		b1.getDstPort();
		b1.toString();

		// equals
		assertFalse(b1.equals(null));
		assertTrue(b1.equals(b1));
		assertFalse(b1.equals(this));
		assertTrue(b1.equals(b2));

		// compareTo
		assertTrue(b1.compareTo(b2) == 0);

		// srcportは無視
		b2 = new FwLogBean("2018-04-12", "00:00:45", "notice", "172.30.90.69", "0", "128.221.236.246", "443");
		assertTrue(b1.compareTo(b2) == 0);
		assertTrue(b1.equals(b2));

		// 大小比較
		b2 = new FwLogBean("2018-04-12", "00:00:45", "notice", "172.30.90.69", "43692", "128.221.236.246", "442");
		assertTrue(b1.compareTo(b2) < 0);
		assertFalse(b1.equals(b2));
		b2 = new FwLogBean("2018-04-12", "00:00:45", "notice", "172.30.90.69", "43692", "128.221.236.246", "444");
		assertTrue(b1.compareTo(b2) > 0);
		assertFalse(b1.equals(b2));
		b2 = new FwLogBean("2018-04-12", "00:00:45", "notice", "172.30.90.68", "43692", "128.221.236.246", "443");
		assertTrue(b1.compareTo(b2) < 0);
		assertFalse(b1.equals(b2));
		b2 = new FwLogBean("2018-04-12", "00:00:45", "notice", "172.30.90.70", "43692", "128.221.236.246", "443");
		assertTrue(b1.compareTo(b2) > 0);
		assertFalse(b1.equals(b2));
		b2 = new FwLogBean("2018-04-12", "00:00:45", "notice", "172.30.90.69", "43692", "128.221.236.245", "443");
		assertTrue(b1.compareTo(b2) < 0);
		assertFalse(b1.equals(b2));
		b2 = new FwLogBean("2018-04-12", "00:00:45", "notice", "172.30.90.69", "43692", "128.221.236.247", "443");
		assertTrue(b1.compareTo(b2) > 0);
		assertFalse(b1.equals(b2));
	}

}
