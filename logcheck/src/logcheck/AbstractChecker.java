package logcheck;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
//import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.inject.Inject;

import logcheck.annotations.WithElaps;
import logcheck.util.net.NetAddr;

/*
 * アクセスログのソースIPに一致するISP名/企業名を取得し、国別にISP名/企業名と出力ログ数を出力する
 * 第1引数：ISP別 IPアドレスリスト
 * 第2引数：インターネット経由接続先一覧
 * 第3引数以降：アクセスログ
 */
public abstract class AbstractChecker<T> implements Callable<T> {

	@Inject private Logger log;

	private Stream<String> stream;

	protected final Set<String> userErrs = new TreeSet<>(); 
	protected final Set<NetAddr> addrErrs = new TreeSet<>(); 

	/*
			Pattern.compile(""),
	 */
	protected static final Pattern[] FAIL_PATTERNS = {
			Pattern.compile("Host Checker policy 'MAC_Address_Filter' failed on host .+"),	// 単独で発生
			Pattern.compile("Login failed using auth server NSSDC_LDAP \\(LDAP Server\\).  Reason: Failed"),		// 後："Primary authentication failed for [\\S ]+ from \\S+"
			Pattern.compile("Login failed using auth server NSSDC_LDAP \\(LDAP Server\\).  Reason: Short Password"),//　後："Testing Password realm restrictions failed for [\\S ]+ , with certificate '[\\w ,=-]+' *"
			Pattern.compile("Login failed.  Reason: Failed"),								// 単独で発生
			Pattern.compile("Login failed.  Reason: No Certificate"),						// 後："Testing Certificate realm restrictions failed for [\\w\\.]*/NSSDC-Auth(1|2)(\\(MAC\\))? *"
			Pattern.compile("Login failed.  Reason: No Roles"),								// 単独
			Pattern.compile("Login failed.  Reason: Revoked Certificate"),					//　後："Testing Certificate realm restrictions failed for [\\w\\.]*/NSSDC-Auth(1|2)(\\(MAC\\))? , with certificate '[\\w ,=-]+' *"
			Pattern.compile("Testing Source IP realm restrictions failed for /NSSDC-Auth1 *"),	// 単独or後："Login failed.  Reason: IP Denied"
	};
	protected static final Pattern[] FAIL_PATTERNS_DUP = {
			Pattern.compile(" authentication failed for Primary/\\w+  from NSSDC_LDAP"),
			Pattern.compile("Login failed \\(NSSDC_LDAP\\).  Reason: LDAP Server"),			// 後： authentication failed for Primary/Z06290  from NSSDC_LDAP
			Pattern.compile("Login failed.  Reason: IP Denied"),							// 前："Testing Source IP realm restrictions failed for \\w+/NSSDC-Auth1 *"
			Pattern.compile("Primary authentication failed for [\\S ]+ from \\S+"),
			Pattern.compile("Testing Certificate realm restrictions failed for [\\w\\.@ ]*/NSSDC-Auth(1|2)(\\(MAC\\))? *"),
			Pattern.compile("Testing Certificate realm restrictions failed for [\\w\\.@ ]*/NSSDC-Auth(1|2)(\\(MAC\\))? , with certificate '[\\w ,=-]+' *"),
			Pattern.compile("Testing Password realm restrictions failed for [\\S ]+ , with certificate '[\\w ,=-]+' *"),
			Pattern.compile("Testing Source IP realm restrictions failed for \\w+/NSSDC-Auth1 *"),	// 後："Login failed.  Reason: IP Denied"
			Pattern.compile("The X\\.509 certificate for .+; Detail: 'certificate revoked' "),
			Pattern.compile("TLS handshake failed - client issued alert 'untrusted or unknown certificate'"),
	};
	protected static final Pattern[] INFO_PATTERNS = {
			Pattern.compile("Active user '\\S+' in realm 'NSSDC-Auth(1|2)(\\(MAC\\))?' is deleted since user does not qualify reevaluated policies"),
			Pattern.compile("Agent login succeeded for \\S+/NSSDC-Auth(1|2)(\\(MAC\\))? from [\\d\\.]+ with Junos-Pulse/[\\d\\.]+ \\([\\w\\. ]+\\) Pulse/[\\d\\.]+\\."),
			Pattern.compile("Agent login succeeded for \\S+/NSSDC-Auth(1|2)(\\(MAC\\))? from [\\d\\.]+ with Pulse-Secure/[\\d\\.]+ \\([\\w\\. ]+\\) Pulse/[\\d\\.]+\\."),
			Pattern.compile("Certificate realm restrictions successfully passed for [\\S ]+ , with certificate '[\\S ]+'"),
			Pattern.compile("Closed connection to [\\d\\.]+ after \\d+ seconds, with -?\\d+ bytes read and -?\\d+ bytes written "),
			Pattern.compile("CRL checking started for certificate '[\\S ]+' issued by [\\S ]+"),
			Pattern.compile("Host Checker running on host [\\d\\.]+ will exit as the user login timed out\\."),
			Pattern.compile("Host Checker policy 'MAC_Address_Filter' passed on host '[\\d\\.]*' address '[\\w-]*'  for user '[\\w\\.]+'\\."),
			Pattern.compile("Host Checker policy 'MAC_Address_Filter' passed on host [\\d\\.]+ ( for user '\\w+')?."),
			Pattern.compile("Host Checker realm restrictions successfully passed for \\S+/NSSDC-Auth(1|2)(\\(MAC\\))? , with certificate '[\\w ,=-]+'"),
			Pattern.compile("Key Exchange number \\d+ occurred for user with NCIP [\\d\\.]+ "),
			Pattern.compile("Login succeeded for \\S+/NSSDC-Auth(1|2)(\\(MAC\\))? \\(session:\\d+\\) from [\\d\\.]+\\."),
			Pattern.compile("Logout from [\\d\\.]+ \\(session:\\d+\\)"),
			Pattern.compile("Max session timeout for \\S+/NSSDC-Auth(1|2)(\\(MAC\\))? \\(session:\\d+\\)\\."),
			Pattern.compile("Primary authentication successful for [\\S ]+ from [\\d\\.]+"),
			Pattern.compile("Remote address for user \\S+/NSSDC-Auth(1|2)(\\(MAC\\))? changed from [\\d\\.]+ to [\\d\\.]+\\."),
			Pattern.compile("Session for user \\w+ on host [\\d\\.]+ has been terminated\\."),
			Pattern.compile("Session resumed from user agent 'Junos-Pulse/[\\d\\.]+ \\([\\w\\. ]+\\) Pulse/[\\d\\.]+'"),
			Pattern.compile("Session resumed from user agent 'Pulse-Secure/[\\d\\.]+ \\([\\w\\. ]+\\) Pulse/[\\d\\.]+'"),
			Pattern.compile("Session timed out for \\S+/NSSDC-Auth(1|2)(\\(MAC\\))? \\(session:\\d+\\) due to inactivity \\(last access at [\\d:]+ [\\d/]+\\)\\. Idle session identified after user request\\."),
			Pattern.compile("Session timed out for \\S+/NSSDC-Auth(1|2)(\\(MAC\\))? \\(session:\\d+\\) due to inactivity \\(last access at [\\d:]+ [\\d/]+\\)\\. Idle session identified during routine system scan\\."),
			Pattern.compile("Source IP realm restrictions successfully passed for [\\S ]+ "),
			Pattern.compile("Source IP realm restrictions successfully passed for [\\S ]+ , with certificate '[\\S ]+'"),
			Pattern.compile("System process detected a Host Checker time out on host [\\d\\.]+  for user '\\w+'  \\(last update at [\\d-]+ [\\d\\.]+ \\+\\d+ JST\\)\\."),
			Pattern.compile("The X\\.509 certificate for '[\\S ]+' issued by [\\S ]+, successfully passed CRL checking"),
			Pattern.compile("Transport mode switched over to SSL for user with NCIP [\\d\\.]+ "),
			Pattern.compile("VPN Tunneling: ACL count = \\d+\\."),
			Pattern.compile("VPN Tunneling: Optimized ACL count = \\d+\\."),
			Pattern.compile("VPN Tunneling: Session ended for user with IPv4 address [\\d\\.]+"),
			Pattern.compile("VPN Tunneling: Session started for user with IPv4 address [\\d\\.]+, hostname [\\w\\.-]+"),
			Pattern.compile("VPN Tunneling: User with IP [\\d\\.]+ connected with (ESP|SSL) transport mode\\. "),
			Pattern.compile("Warning! Number of concurrent users \\(\\d+\\) is nearing the system limit \\(\\d+\\)\\."),
			Pattern.compile("Warning! Number of concurrent users is nearing the system limit \\(\\d+\\)\\."),
			Pattern.compile("\\S+/NSSDC-Auth(1|2)(\\(MAC\\))? logged out from IP \\([\\d\\.]+\\) because user started new session from IP \\([\\d\\.]+\\)\\."),
	};
	protected static final String INFO_SUMMARY_MSG = "<><><> Information message summary <><><>";

	protected AbstractChecker() { }

	private T run(InputStream is) throws Exception {
		log.info("checking from InputStream:");

		this.stream = new BufferedReader(new InputStreamReader(is)).lines();
		T map = run2();
		return map;
	}
	private T run(String file) throws Exception {
		log.info("checking from file=" + file + ":");

		this.stream = Files.lines(Paths.get(file), StandardCharsets.UTF_8);
		T map = run2();
		return map;
	}
	private T run2() throws Exception {

		ExecutorService exec = null;
		T map = null;
		try {
			exec = Executors.newFixedThreadPool(2);
			CheckProgress p = new CheckProgress();

			Future<T> f1 = exec.submit(this);
			exec.execute(p);

			map = f1.get();

			p.stopRequest();
		}
		finally {
			exec.shutdown();
		}
		return map;
	}

	protected abstract T call(Stream<String> stream) throws Exception;
	protected abstract void report(final PrintWriter out, final T map);

	@Override @WithElaps
	public T call() throws Exception {
		return call(stream);
	}

	// 将来的にサブクラス外からの呼び出しを考慮してpublicとする
	@WithElaps
	public void start(String[] argv, int offset) throws Exception {
		T map = null;
		if (argv.length <= offset) {
			map = run(System.in);
		}
		else {
			for (int ix = offset; ix < argv.length; ix++ ) {
				map = run(argv[ix]);
			}
		}
		addrErrs.forEach(addr -> log.warning("unknown ip: addr=" + addr));
		userErrs.forEach(userId -> log.warning("not found user: userid=" + userId));

		String crlf = System.getProperty("line.separator");
		System.setProperty("line.separator", "\r\n");

		try (PrintWriter out = new PrintWriter(System.out))
		{
			report(out, map);
			out.flush();
		}
		System.setProperty("line.separator", crlf);
	}

	private class CheckProgress implements Runnable {
		
		private boolean stopRequest = false;

		public void run() {
			while (!stopRequest) {
				try {
					Thread.sleep(1000);
					System.err.print(".");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
			}
		}
		
		public void stopRequest() {
			stopRequest = true;
		}
	}

}
