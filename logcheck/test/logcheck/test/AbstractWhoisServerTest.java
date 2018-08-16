package logcheck.test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.known.KnownListIsp;
import logcheck.known.impl.AbstractWhoisServer;
import logcheck.known.impl.net.WhoisApnic;
import logcheck.known.impl.net.WhoisArin;
import logcheck.known.impl.net.WhoisLacnic;
import logcheck.known.impl.net.WhoisTreetCoJp;
import logcheck.util.NetAddr;

public class AbstractWhoisServerTest {

	@BeforeClass
	public static void beforeClass() {
		System.out.println("start AbstractWhoisServerTest ...");
		Env.init();
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("AbstractWhoisServerTest ... end");
	}

	@Test
	public void test1() throws InterruptedException {
		WhoisLacnic lacnic = new WhoisLacnic();
		WhoisTreetCoJp treet = new WhoisTreetCoJp();
		WhoisArin arin = new WhoisArin();
		WhoisApnic apnic = new WhoisApnic();

		ExecutorService exec = Executors.newFixedThreadPool(3);
		final AbstractWhoisServer[] whois = { treet, arin, apnic, lacnic };
		Stream.of(whois).forEach(w -> {
			w.init();
			w.setAddr(new NetAddr("203.118.54.210"));
		});
		List<Future<KnownListIsp>> list = exec.invokeAll(Arrays.asList(whois), 30, TimeUnit.SECONDS);
		list.forEach(f -> {
			KnownListIsp isp;
			try {
				isp = f.get();
				System.out.println(isp);
			}
			catch (InterruptedException | ExecutionException e) {
				System.out.println(e);
			}
		});
	}

}
