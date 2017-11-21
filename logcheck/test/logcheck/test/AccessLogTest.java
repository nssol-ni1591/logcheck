package logcheck.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Stream;

import logcheck.log.AccessLog;
import logcheck.log.AccessLogSummary;

import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.*;
import static org.junit.Assume.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class AccessLogTest {

	@BeforeClass
	public static void beforeClass() {
		System.out.println("start AccessLogTest ...");
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("AccessLogTest ... end");
	}

	public HashMap<String, AccessLogSummary> load(String file) {
		HashMap<String, AccessLogSummary> map = new HashMap<>();
		try {
				System.out.println("AccessLogTest.load ... file=" + file);

				try (Stream<String> input = Files.lines(Paths.get(file), StandardCharsets.UTF_8)) {
					input.filter(AccessLog::test)
						.map(AccessLog::parse)
						.forEach(b -> {
							AccessLogSummary als = map.get(b.getAddr().toString());
							if (als == null) {
								als = new AccessLogSummary(b, null);
								map.put(b.getAddr().toString(), als);
							}
						});
				};
				System.out.println("end AccessLogTest.load ...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}

	@Test
	public void test01() {
		HashMap<String, AccessLogSummary> map = new HashMap<>();
		map = load("C:\\Users\\NI1591\\Desktop\\2017-セキュリティ対策\\VPN-LOG\\20171021.log");
		System.out.println("size=" + map.size());
		assumeTrue("log's count is 73", map.size() == 73);
		
		for (AccessLogSummary sum : map.values()) {
			assertNotNull("getAddr() is null", sum.getAddr());
			assertNotNull("getAfterUsrId() is null", sum.getAfterUsrId());
			assertNotNull("getCount() is null", sum.getCount());
			assertNotNull("getDetail() is null", sum.getDetail());
			assertNotNull("getFirstDate() is null", sum.getFirstDate());
			assertNotNull("getId() is null", sum.getId());
			sum.getIsp();
			assertNotNull("getLastDate() is null", sum.getLastDate());
			sum.getPattern();
			assertNotNull("getReason() is null", sum.getReason());
			assertNotNull("getRoles() is null", sum.getRoles());
		}
	}
}
