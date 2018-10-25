package logcheck.test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
		// システムプロパティの設定は早めに行う
		Env.init();
		System.out.println("start AbstractWhoisServerTest ...");
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("AbstractWhoisServerTest ... end");
	}

	@Test
	public void test1() throws InterruptedException {
		ExecutorService exec = Executors.newFixedThreadPool(3);
		String addr = "203.118.54.210";
		AsyncWhois[] whois = { 
				new AsyncWhois(new WhoisApnic(), addr),
				new AsyncWhois(new WhoisLacnic(), addr),
				new AsyncWhois(new WhoisTreetCoJp(), addr),
				new AsyncWhois(new WhoisArin(), addr),
		};
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
	class AsyncWhois implements Callable<KnownListIsp> {

		private final AbstractWhoisServer whois;
		private final NetAddr addr;

		AsyncWhois(AbstractWhoisServer whois, String addr) {
			this.whois = whois;
			this.addr = new NetAddr(addr);
		}
		
		@Override
		public KnownListIsp call() throws Exception {
			whois.init();
			whois.setAddr(addr);

			long time = System.currentTimeMillis();
			KnownListIsp isp = whois.call();
			time = System.currentTimeMillis() - time;
			System.out.println(whois.getName() + ": isp=\"" + isp + "\", elaps=" + time + "ms");
			return isp;
		}

	}

}
