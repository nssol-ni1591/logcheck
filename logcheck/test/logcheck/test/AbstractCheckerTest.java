package logcheck.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.stream.Stream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import logcheck.AbstractChecker;
import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.site.impl.DbSiteList;
import logcheck.util.NetAddr;

public class AbstractCheckerTest extends AbstractChecker<String> {

	private KnownList knownlist = null;
	private DbSiteList maglist = new DbSiteList();

	@BeforeClass
	public static void beforeClass() {
		System.out.println("start AbstractCheckerTest ...");
	}
	@AfterClass
	public static void afterClass() {
		System.out.println("AbstractCheckerTest ... end");
	}

	@Override
	public void init(String... argv) throws IOException, ClassNotFoundException, SQLException {
		//this.knownlist.load(argv[0])
	}

	@Override
	protected String call(Stream<String> stream) {
		return null;
	}

	@Override
	protected void report(PrintWriter out, String map) {
		// Do nothing
	}

	@Test
	public void test1() throws ClassNotFoundException, IOException, SQLException {
		this.maglist.init();
		this.maglist.load(Env.KNOWNLIST);

		IspList isp = getIsp(null, maglist, knownlist);
		System.out.println("getIsp(null): " + isp);

		isp = getIsp(new NetAddr("260.1.1.1"), maglist, knownlist);
		System.out.println("getIsp(260.1.1.1): " + isp);
	}
}
