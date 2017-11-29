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
	public static void beforeClass() {
		System.err.println("start FwLogTest ... ");
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
				prevSummary = summary;
		});
		return map;
	}

	@Test
	public void test01() throws IOException {
		Map<FwLogBean, FwLogSummary> map = null;
		map = load("C:\\Users\\NI1591\\Desktop\\2017-セキュリティ対策\\fortigate-LOG\\ForwardTrafficLog-disk-2017-04-21T14-38-26.394251.log");
		//map.values().forEach(summary -> System.out.println("log: " + summary));

		// 正常動作なので、ログのコレクションが返却されて正常
		assertFalse(map.isEmpty());
	}

	@Test(expected = NoSuchFileException.class)
	public void test02() throws IOException {
		//Map<FwLogBean, FwLogSummary> map = null;
		load("a.txt");
		// 存在しないファイルを指定しているので、例外発生が正常
		fail("throw exception?");
	}
}
