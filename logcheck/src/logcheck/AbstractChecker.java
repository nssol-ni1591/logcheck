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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.inject.Inject;

import logcheck.annotations.WithElaps;
import logcheck.util.net.NetAddr;
import logcheck.util.weld.WeldRunner;

/*
 * アクセスログのソースIPに一致するISP名/企業名を取得し、国別にISP名/企業名と出力ログ数を出力する
 * 第1引数：ISP別 IPアドレスリスト
 * 第2引数：インターネット経由接続先一覧
 * 第3引数以降：アクセスログ
 */
public abstract class AbstractChecker<T> implements Callable<T>, WeldRunner {

	@Inject private Logger log;

	private Stream<String> stream;

	protected final Set<String> projErrs = new TreeSet<>(); 
	protected final Set<String> userErrs = new TreeSet<>(); 
	protected final Set<NetAddr> addrErrs = new TreeSet<>(); 

	/*
			Pattern.compile(""),
	 */
	protected static final Pattern[] FAIL_PATTERNS = {
			Pattern.compile("Account disabled by password management on auth server '[\\S]+'"),	// 前：Primary authentication failed for ...
			Pattern.compile("Host Checker policy 'MAC_Address_Filter' failed on host .+"),	// 単独で発生
			Pattern.compile("Login failed using auth server (NSSDC_LDAP|SDC-AD) \\([\\w ]+\\)\\.  Reason: Failed"),		// 後："Primary authentication failed for [\\S ]+ from \\S+"
			Pattern.compile("Login failed using auth server (NSSDC_LDAP|SDC-AD) \\([\\w ]+\\)\\.  Reason: Short Password"),//　後："Testing Password realm restrictions failed for [\\S ]+ , with certificate '[\\w ,=-]+' *"
			Pattern.compile("Login failed using auth server NSSDC-Auth3\\(AD\\)\\.  Reason: SDC-AD"),		// 後："Primary authentication failed for [\\S ]+ from \\S+"
			Pattern.compile("Login failed.  Reason: Failed"),								// 単独で発生
			Pattern.compile("Login failed.  Reason: IP Denied"),							// 前："Testing Source IP realm restrictions failed for \\w+/NSSDC-Auth1 *"
			Pattern.compile("Login failed.  Reason: No Certificate"),						// 後："Testing Certificate realm restrictions failed for [\\w\\.]*/NSSDC-Auth(1|2)(\\(MAC\\))? *"
			Pattern.compile("Login failed.  Reason: No Roles"),								// 単独
			Pattern.compile("Login failed.  Reason: Revoked Certificate"),					//　後："Testing Certificate realm restrictions failed for [\\w\\.]*/NSSDC-Auth(1|2)(\\(MAC\\))? , with certificate '[\\w ,=-]+' *"
//			Pattern.compile("Login failed.  Reason: Revoked SDC-AD"),						//　後："NSSDC-Auth3(AD) authentication failed for /Primary from ..."
			Pattern.compile("Login failed.  Reason: Wrong Certificate::unable to get certificate CRL"),	//　2017-10-26追加: 後："Testing Certificate realm restrictions failed for [\\w\\.]*/NSSDC-Auth(1|2)(\\(MAC\\))? , with certificate '[\\w ,=-]+' unable to get certificate CRL"
			Pattern.compile("Login failed \\((NSSDC_LDAP|SDC-AD)\\)\\.  Reason: (LDAP Server|SDC-AD|Active Directory)"),			// 後： authentication failed for Primary/Z06290  from NSSDC_LDAP
			Pattern.compile("Could not connect to LDAP server '(NSSDC_LDAP|SDC-AD)': Failed binding to admin DN: \\[\\d+\\] Can't contact LDAP server: [\\d\\.:]+ [\\d\\.:]+"),
	};
	protected static final Pattern[] FAIL_PATTERNS_DUP = {
//			Pattern.compile("Active Directory authentication server '[\\S]+' : Received NTSTATUS code 'STATUS_ACCOUNT_DISABLED' \\."),
//			Pattern.compile("Active Directory authentication server '[\\S]+' : Received NTSTATUS code 'STATUS_WRONG_PASSWORD' \\."),
			Pattern.compile("Active Directory authentication server '[\\S]+' : Received NTSTATUS code '[\\w_]+' \\."),
			Pattern.compile("Authentication failure for AD server '[\\S]+': specified account does not exist"),	// => Login failed using auth server SDC-AD (Active Directory).  Reason: Failed
			Pattern.compile("Host Checker policies could not be evaluated on host '[\\d\\.]+' address '[\\w\\-]+'."),	// 後：Host Checker policy ...
			Pattern.compile("(Primary|NSSDC-Auth3\\(AD\\))? authentication failed for [\\S ]+ from [\\S ]+"),
//			Pattern.compile("(NSSDC-Auth3\\(AD\\))? authentication failed for /Primary from [\\S ]+"),
//			Pattern.compile("Testing Certificate realm restrictions failed for [\\S ]*/NSSDC-Auth(1|2\\(MAC\\)|3\\(AD\\)|4\\(AD_MAC\\)) *"),
//			Pattern.compile("Testing Certificate realm restrictions failed for [\\S ]*/NSSDC-Auth(1|2\\(MAC\\)|3\\(AD\\)|4\\(AD_MAC\\)) , with certificate '[\\w ,=-]+' *"),
			Pattern.compile("Testing Certificate realm restrictions failed for [\\S ]*/NSSDC-Auth(1|2\\(MAC\\)|3\\(AD\\)|4\\(AD_MAC\\)) (, with certificate '[\\w ,=-]+')? "),
//			Pattern.compile("Testing Password realm restrictions failed for [\\S ]*/NSSDC-Auth(1|2\\(MAC\\)|3\\(AD\\)|4\\(AD_MAC\\)) *"),
//			Pattern.compile("Testing Password realm restrictions failed for [\\S ]*/NSSDC-Auth(1|2\\(MAC\\)|3\\(AD\\)|4\\(AD_MAC\\)) , with certificate '[\\w ,=-]+' *"),
			Pattern.compile("Testing Password realm restrictions failed for [\\S ]*/NSSDC-Auth(1|2\\(MAC\\)|3\\(AD\\)|4\\(AD_MAC\\)) (, with certificate '[\\w ,=-]+')? "),
//			Pattern.compile("Testing Source IP realm restrictions failed for /NSSDC-Auth1 *"),	// 後："Login failed.  Reason: IP Denied"
			Pattern.compile("Testing Source IP realm restrictions failed for [\\S ]*/NSSDC-Auth(1|3|3\\(AD\\)) *"),	// 後："Login failed.  Reason: IP Denied"
//			Pattern.compile("The X\\.509 certificate for .+; Detail: 'certificate revoked' "),
//			Pattern.compile("The X\\.509 certificate for .+; Detail: 'unable to get certificate CRL' "),	// 2017-10-26追加
			Pattern.compile("The X\\.509 certificate for .+; Detail: '[\\w ]+' "),
			Pattern.compile("TLS handshake failed - client issued alert 'untrusted or unknown certificate'"),
	};
	protected static final Pattern[] INFO_PATTERNS = {
			Pattern.compile("Active user '\\S+' in realm 'NSSDC-Auth(1|2\\(MAC\\)|3\\(AD\\)|4\\(AD_MAC\\))' is deleted since user does not qualify reevaluated policies"),
//			Pattern.compile("Agent login succeeded for \\S+/NSSDC-Auth\\d(\\(MAC\\))? from [\\d\\.]+ with Pulse-Secure/[\\d\\.]+ \\([\\w\\. ]+\\) Pulse/[\\d\\.]+\\."),
//			Pattern.compile("Agent login succeeded for \\S+/NSSDC-Auth\\d(\\(MAC\\))? from [\\d\\.]+ with Junos-Pulse/[\\d\\.]+ \\([\\w\\. ]+\\) Pulse/[\\d\\.]+\\."),
			Pattern.compile("Agent login succeeded for \\S+/NSSDC-Auth(1|2\\(MAC\\)|3\\(AD\\)|4\\(AD_MAC\\)) from [\\d\\.]+ with Pulse-Secure/[\\d\\.]+ \\([\\w\\. ]+\\) Pulse/[\\d\\.]+\\."),
			Pattern.compile("Agent login succeeded for \\S+/NSSDC-Auth(1|2\\(MAC\\)|3\\(AD\\)|4\\(AD_MAC\\)) from [\\d\\.]+ with Junos-Pulse/[\\d\\.]+ \\([\\w\\. ]+\\) Pulse/[\\d\\.]+\\."),
			Pattern.compile("Certificate realm restrictions successfully passed for [\\S ]+ , with certificate '[\\S ]+'"),
			Pattern.compile("Closed connection to [\\d\\.]+ after \\d+ seconds, with -?\\d+ bytes read and -?\\d+ bytes written "),
			Pattern.compile("CRL checking started for certificate '[\\S ]+' issued by [\\S ]+"),
			Pattern.compile("Host Checker running on host [\\d\\.]+ will exit as the user login timed out\\."),
			Pattern.compile("Host Checker policy 'MAC_Address_Filter' passed on host '[\\d\\.]+' address '[\\w-]*'  for user '[\\S ]+'\\."),
			Pattern.compile("Host Checker policy 'MAC_Address_Filter' passed on host [\\d\\.]+ ( for user '\\w+')?."),
			Pattern.compile("Host Checker realm restrictions successfully passed for \\S+/NSSDC-Auth(1|2\\(MAC\\)|3\\(AD\\)|4\\(AD_MAC\\)) (, with certificate '[\\w ,=-]+')?"),
			Pattern.compile("Key Exchange number \\d+ occurred for user with NCIP [\\d\\.]+ "),
			Pattern.compile("Login succeeded for \\S+/NSSDC-Auth(1|2\\(MAC\\)|3\\(AD\\)|4\\(AD_MAC\\)) \\(session:\\d+\\) from [\\d\\.]+\\."),
			Pattern.compile("Logout from [\\d\\.]+ \\(session:\\d+\\)"),
			Pattern.compile("Max session timeout for \\S+/NSSDC-Auth(1|2\\(MAC\\)|3\\(AD\\)|4\\(AD_MAC\\)) \\(session:\\d+\\)\\."),
			Pattern.compile("Primary authentication successful for [\\S ]+ from [\\d\\.]+"),
			Pattern.compile("Remote address for user \\S+/NSSDC-Auth(1|2\\(MAC\\)|3\\(AD\\)|4\\(AD_MAC\\)) changed from [\\d\\.]+ to [\\d\\.]+\\.( Access denied\\.)?"),
			Pattern.compile("Session for user \\S+ on host [\\d\\.]+ has been terminated\\."),
			Pattern.compile("Session resumed from user agent 'Junos-Pulse/[\\d\\.]+ \\([\\w\\. ]+\\) Pulse/[\\d\\.]+'"),
			Pattern.compile("Session resumed from user agent 'Pulse-Secure/[\\d\\.]+ \\([\\w\\. ]+\\) Pulse/[\\d\\.]+'"),
			Pattern.compile("Session timed out for \\S+/NSSDC-Auth(1|2\\(MAC\\)|3\\(AD\\)|4\\(AD_MAC\\)) \\(session:\\d+\\) due to inactivity \\(last access at [\\d:]+ [\\d/]+\\)\\. Idle session identified after user request\\."),
			Pattern.compile("Session timed out for \\S+/NSSDC-Auth(1|2\\(MAC\\)|3\\(AD\\)|4\\(AD_MAC\\)) \\(session:\\d+\\) due to inactivity \\(last access at [\\d:]+ [\\d/]+\\)\\. Idle session identified during routine system scan\\."),
			Pattern.compile("Source IP realm restrictions successfully passed for [\\S ]+ "),
			Pattern.compile("Source IP realm restrictions successfully passed for [\\S ]+ , with certificate '[\\S ]+'"),
			Pattern.compile("System process detected a Host Checker time out on host [\\d\\.]+  for user '\\S+'  \\(last update at [\\d-]+ [\\d\\.]+ \\+\\d+ JST\\)\\."),
			Pattern.compile("The X\\.509 certificate for '[\\S ]+' issued by [\\S ]+, successfully passed CRL checking"),
			Pattern.compile("Transport mode switched over to SSL for user with NCIP [\\d\\.]+ "),
			Pattern.compile("VPN Tunneling: ACL count = \\d+\\."),
			Pattern.compile("VPN Tunneling: Optimized ACL count = \\d+\\."),
			Pattern.compile("VPN Tunneling: Session ended for user with IPv4 address [\\d\\.]+"),
			Pattern.compile("VPN Tunneling: Session started for user with IPv4 address [\\d\\.]+, hostname [\\w\\.-]+"),
			Pattern.compile("VPN Tunneling: User with IP [\\d\\.]+ connected with (ESP|SSL) transport mode\\. "),
			Pattern.compile("Warning! Number of concurrent users \\(\\d+\\) is nearing the system limit \\(\\d+\\)\\."),
			Pattern.compile("Warning! Number of concurrent users is nearing the system limit \\(\\d+\\)\\."),
			Pattern.compile("\\S+/NSSDC-Auth(1|2\\(MAC\\)|3\\(AD\\)|4\\(AD_MAC\\)) logged out from IP \\([\\d\\.]+\\) because user started new session from IP \\([\\d\\.]+\\)\\."),
	};
	protected static final String INFO_SUMMARY_MSG = "<><><> Information message summary <><><>";
	protected static final String DUP_FAILED_MSG = "<><><> Duplicate failed message summary <><><>";

	private static final String LINE_SEPARATOR = "line.separator";
	
	protected AbstractChecker() {
	}

	private T run(InputStream is) throws Exception {
		log.info("checking from InputStream:");

		this.stream = new BufferedReader(new InputStreamReader(is)).lines();
		return run2();
	}
	private T run(String file) throws Exception {
		log.log(Level.INFO, "checking from file={0}:", file);

		this.stream = Files.lines(Paths.get(file), StandardCharsets.UTF_8);
		return run2();
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
			if (exec != null) {
				exec.shutdown();
			}
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
	public int start(String[] argv, int offset) throws Exception {
		int rc = 0;
		T map = null;
		if (argv.length <= offset) {
			map = run(System.in);
		}
		else {
			for (int ix = offset; ix < argv.length; ix++ ) {
				map = run(argv[ix]);
			}
		}

		addrErrs.forEach(addr -> log.log(Level.WARNING, "unknown ip: addr={0}", addr));
		userErrs.forEach(userId -> log.log(Level.WARNING, "not found user: userid={0}", userId));
		projErrs.forEach(proj -> log.log(Level.WARNING, "unknown proj: proj={0}", proj));

		// 結果の出力。ただし、CSV形式とするため改行コードを"\r\n"に変更する
		String crlf = System.getProperty(LINE_SEPARATOR);
		System.setProperty(LINE_SEPARATOR, "\r\n");
		try (PrintWriter out = new PrintWriter(System.out)) {
			report(out, map);
			out.flush();
		}
		System.setProperty(LINE_SEPARATOR, crlf);
		return rc;
	}

	private class CheckProgress implements Runnable {
		
		private boolean stopRequest = false;

		public void run() {
			while (!stopRequest) {
				try {
					Thread.sleep(1000);
					System.err.print(".");
				} catch (InterruptedException e) {
					// もし例外が発生してしまうとスレッドが停止してしまうが
					// sonarのパーサが文句を言うので仕方がない
					Thread.currentThread().interrupt();
				}
			}
		}
		
		public void stopRequest() {
			stopRequest = true;
		}
	}

}
