package logcheck;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import logcheck.annotations.WithElaps;
import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.mag.MagList;
import logcheck.util.NetAddr;
import logcheck.util.WeldRunner;

/*
 * アクセスログのソースIPに一致するISP名/企業名を取得し、国別にISP名/企業名と出力ログ数を出力する
 * 第1引数：ISP別 IPアドレスリスト
 * 第2引数：インターネット経由接続先一覧
 * 第3引数以降：アクセスログ
 */
public abstract class AbstractChecker<T> implements WeldRunner {

	@Inject private Logger log;

	protected final Set<String> projErrs = new TreeSet<>(); 
	protected final Set<String> userErrs = new TreeSet<>(); 
	protected final Set<NetAddr> addrErrs = new TreeSet<>(); 
	protected final Set<String> ptnErrs = new TreeSet<>(); 

	// 一次エラーメッセージ
	protected static final Pattern[] FAIL_PATTERNS;
	// 二次以降のエラーメッセージ
	protected static final Pattern[] FAIL_PATTERNS_DUP;
	// 全てのエラーメッセージ
	protected static final Pattern[] FAIL_PATTERNS_ALL;
	// 情報メッセージ
	protected static final Pattern[] INFO_PATTERNS;
	// 全てのメッセージ
	protected static final Pattern[] ALL_PATTERNS;

	protected static final Pattern[] FAIL_PATTERNS_PART = {
			Pattern.compile("Account disabled by password management on auth server '[\\S]+'"),	// 前：Primary authentication failed for ...
			Pattern.compile("Authentication rejected. Reason: Timed out while waiting for client certificate"),
			Pattern.compile("Host Checker policy '[\\w_]+' failed on host .+"),	// 単独で発生
			Pattern.compile("Login failed using auth server NSSDC-Auth3\\(\\w+\\)\\.  Reason: SDC-AD"),		// 2017-12-01 1回のみ
			Pattern.compile("Login failed.  Reason: Failed"),								// 単独で発生
			Pattern.compile("Login failed.  Reason: IP Denied"),							// 前："Testing Source IP realm restrictions failed for \\w+/NSSDC-Auth1 *"
			Pattern.compile("Login failed.  Reason: No Certificate"),						// 後："Testing Certificate realm restrictions failed for [\\w\\.]*/NSSDC-Auth(1|2)(\\(MAC\\))? *"
			Pattern.compile("Login failed.  Reason: No Roles"),								// 単独
			Pattern.compile("Login failed.  Reason: Revoked Certificate"),					//　後："Testing Certificate realm restrictions failed for [\\w\\.]*/NSSDC-Auth(1|2)(\\(MAC\\))? , with certificate '[\\w ,=-]+' *"
			Pattern.compile("Login failed.  Reason: Wrong Certificate::unable to get certificate CRL"),	//　2017-10-26追加: 後："Testing Certificate realm restrictions failed for [\\w\\.]*/NSSDC-Auth(1|2)(\\(MAC\\))? , with certificate '[\\w ,=-]+' unable to get certificate CRL"
			Pattern.compile("Login failed \\((NSSDC_LDAP|SDC-AD)\\)\\.  Reason: (LDAP Server|SDC-AD|Active Directory)"),			// 後： authentication failed for Primary/Z06290  from NSSDC_LDAP
			Pattern.compile("Could not connect to LDAP server '(NSSDC_LDAP|SDC-AD)': Failed binding to admin DN: \\[\\d+\\] Can't contact LDAP server: [\\d\\.:]+ [\\d\\.:]+"),
	};

	protected static final Pattern[] FAIL_PATTERNS_DUP_PART = {
			Pattern.compile("Active Directory authentication server '[\\S]+' : Received NTSTATUS code '[\\w_]+' \\."),
			Pattern.compile("Authentication failure for AD server '[\\S-]*': bad username or authentication information"),	// => Login failed using auth server SDC-AD (Active Directory).  Reason: Failed
			Pattern.compile("Authentication failure for AD server '[\\S-]*': specified account does not exist"),	// => Login failed using auth server SDC-AD (Active Directory).  Reason: Failed
			Pattern.compile("Host Checker policies could not be evaluated on host '[\\d\\.]+' address '[\\w\\-]+'."),	// 後：Host Checker policy ...
			Pattern.compile("(Primary|NSSDC-Auth3\\(AD\\))? authentication failed for [\\S ]+ from [\\S ]+"),
			Pattern.compile("Testing Certificate realm restrictions failed for [\\S ]*/NSSDC-Auth\\d+(\\([\\w_]+\\))?( , with certificate '[\\w ,=-]+')? *"),
			Pattern.compile("Testing Password realm restrictions failed for [\\S ]*/NSSDC-Auth\\d+(\\([\\w_]+\\))?( , with certificate '[\\w ,=-]+')? *"),
			Pattern.compile("Testing Source IP realm restrictions failed for [\\S ]*/NSSDC-Auth\\d+(\\([\\w_]+\\))? *"),	// 後："Login failed.  Reason: IP Denied"
			Pattern.compile("The X\\.509 certificate for .+; Detail: '[\\w ]+' *"),
			Pattern.compile("TLS handshake failed - (client|server) issued alert '[\\S ]+'"),
	};

	protected static final Pattern[] INFO_PATTERNS_PART = {
			Pattern.compile("Active user '\\S+' in realm 'NSSDC[-_]Auth\\d+(\\([\\w-_]+\\))?' is deleted since user does not qualify reevaluated policies"),
			Pattern.compile("Agent login succeeded for \\S*/NSSDC[-_]Auth\\d(\\([\\w-_]+\\))? from [\\d\\.]+ with [\\w-]+/[\\d\\.]+ \\([\\w\\. ]+\\) Pulse/[\\d\\.]+\\."),
			Pattern.compile("Certificate realm restrictions successfully passed for [\\S ]*/NSSDC[-_]Auth\\d(\\([\\w-_]+\\))?+ , with certificate '[\\S ]+' *"),
			Pattern.compile("Closed connection to [\\d\\.]+ after \\d+ seconds, with -?\\d+ bytes read and -?\\d+ bytes written *"),
			Pattern.compile("CRL checking started for certificate '[\\S ]*' issued by [\\S ]+"),
			Pattern.compile("Host Checker running on host [\\d\\.]+ will exit as the user login timed out\\."),
			Pattern.compile("Host Checker policy '[\\w_]+' passed on host '?[\\d\\.]*'?( address '[\\w\\-]*')? ?( for user '[\\S ]+')?\\."),
			Pattern.compile("Host Checker policies could not be evaluated on host '[\\d\\.]*' address '[\\w\\-]*'."),
			Pattern.compile("Host Checker realm restrictions successfully passed for \\S*/NSSDC[-_]Auth\\d+(\\([\\w-_]+\\))? (, with certificate '[\\w ,=-]+')?"),
			Pattern.compile("Key Exchange number \\d+ occurred for user with NCIP [\\d\\.]+ *"),
			Pattern.compile("Login succeeded for \\S*/NSSDC[-_]Auth\\d+(\\([\\w-_]+\\))? \\(session:\\d+\\) from [\\d\\.]+( with [\\S ]+)?\\."),
			Pattern.compile("Logout from [\\d\\.]+ \\(session:\\d+\\)"),
			Pattern.compile("Max session timeout for \\S*/(NSSDC-Auth\\d+(\\([\\w_]+\\))?)? \\(session:\\d+\\)\\."),
			Pattern.compile("Primary authentication successful for \\S+ from [\\d\\.]+"),
			Pattern.compile("Remote address for user \\S*/NSSDC-Auth\\d+(\\([\\w_]+\\))? changed from [\\d\\.]+ to [\\d\\.]+\\.( Access denied\\.)?"),
			Pattern.compile("Roles for user \\S+ on host [\\d\\.]+ changed from <[\\S ]+> to <[\\S ]+> during policy reevaluation."),
			Pattern.compile("Session for user \\S+ on host [\\d\\.]+ has been terminated\\."),
			Pattern.compile("Session resumed from user agent '[\\w-]+/[\\d\\.]+ \\([\\w\\. ]+\\) Pulse/[\\d\\.]+'"),
			Pattern.compile("Session timed out for \\S*/NSSDC[-_]Auth\\d+(\\([\\w-_]+\\))? \\(session:\\d+\\) due to inactivity \\(last access at [\\d:]+ [\\d/]+\\)\\.[\\S ]*\\."),
			Pattern.compile("Source IP realm restrictions successfully passed for [\\S ]*/NSSDC-Auth\\d+(\\([\\w_]+\\))?( , with certificate '[\\S ]+')? *"),
			Pattern.compile("System process detected a Host Checker time out on host [\\d\\.]+  for user '[\\S ]+'  \\(last update at [\\d-]+ [\\d\\.]+ \\+\\d+ JST\\)\\."),
			Pattern.compile("The X\\.509 certificate for '[\\S ]*' issued by [\\S ]+, successfully passed CRL checking *"),
			Pattern.compile("Transport mode switched over to SSL for user with NCIP [\\d\\.]+ "),
			Pattern.compile("VPN Tunneling: ACL count = \\d+\\."),
			Pattern.compile("VPN Tunneling: Optimized ACL count = \\d+\\."),
			Pattern.compile("VPN Tunneling: Session ended for user with IPv4 address [\\d\\.]+"),
			Pattern.compile("VPN Tunneling: User with IP [\\d\\.]+ connected with (ESP|SSL) transport mode\\. *"),
			Pattern.compile("Warning! Number of concurrent users( \\(\\d+\\))? is nearing the system limit \\(\\d+\\)\\."),
			Pattern.compile("Web request connection from [\\d\\.]+ timed out. Requested URI /\\S*, bytes received \\d+, bytes expected -?\\d+."),
			Pattern.compile("\\S*/(NSSDC-Auth\\d+(\\([\\w_]+\\))?|SDC Admin Users) logged out from IP \\([\\d\\.]+\\) because user started new session from IP \\([\\d\\.]+\\)\\."),
	};

	protected static final Pattern[] AUTH_SUCCESS_PATTERNS = {
			Pattern.compile("Primary authentication successful for [\\S ]+ from [\\d\\.]+"),
	};
	protected static final Pattern[] AUTH_FAILED_PATTERNS = {
			Pattern.compile("Login failed using auth server (NSSDC_LDAP|SDC-AD) \\([\\w ]+\\)\\.  Reason: Failed"),		// 後："Primary authentication failed for [\\S ]+ from \\S+"
			Pattern.compile("Login failed using auth server (NSSDC_LDAP|SDC-AD) \\([\\w ]+\\)\\.  Reason: Short Password"),//　後："Testing Password realm restrictions failed for [\\S ]+ , with certificate '[\\w ,=-]+' *"
	};

	protected static final Pattern IP_RANGE_PATTERN = 
			Pattern.compile("Testing Source IP realm restrictions failed for [\\S ]*/NSSDC-Auth\\d+(\\([\\w_]+\\))? *");
	protected static final Pattern SESS_START_PATTERN = 
			Pattern.compile("VPN Tunneling: Session started for user with IPv4 address ([\\w\\.]+), hostname ([\\S]+)");

	protected static final String INFO_SUMMARY_MSG = "<><><> Information message summary <><><>";
	protected static final String DUP_FAILED_MSG = "<><><> Duplicate failed message summary <><><>";

	private static final String LINE_SEPARATOR = "line.separator";
	
	static {
		FAIL_PATTERNS = new Pattern[FAIL_PATTERNS_PART.length + AUTH_FAILED_PATTERNS.length];
		System.arraycopy(FAIL_PATTERNS_PART, 0, FAIL_PATTERNS, 0, FAIL_PATTERNS_PART.length);
		System.arraycopy(AUTH_FAILED_PATTERNS, 0, FAIL_PATTERNS, FAIL_PATTERNS_PART.length, AUTH_FAILED_PATTERNS.length);

		FAIL_PATTERNS_DUP = new Pattern[FAIL_PATTERNS_DUP_PART.length + 1];
		System.arraycopy(FAIL_PATTERNS_DUP_PART, 0, FAIL_PATTERNS_DUP, 0, FAIL_PATTERNS_DUP_PART.length);
		FAIL_PATTERNS_DUP[FAIL_PATTERNS_DUP_PART.length] = IP_RANGE_PATTERN;

		FAIL_PATTERNS_ALL = new Pattern[FAIL_PATTERNS.length + FAIL_PATTERNS_DUP.length];
		System.arraycopy(FAIL_PATTERNS, 0, FAIL_PATTERNS_ALL, 0, FAIL_PATTERNS.length);
		System.arraycopy(FAIL_PATTERNS_DUP, 0, FAIL_PATTERNS_ALL, FAIL_PATTERNS.length, FAIL_PATTERNS_DUP.length);

		INFO_PATTERNS = new Pattern[INFO_PATTERNS_PART.length + AUTH_SUCCESS_PATTERNS.length + 1 /*SESS_START_PATTERN.length*/];
		System.arraycopy(INFO_PATTERNS_PART, 0, INFO_PATTERNS, 0, INFO_PATTERNS_PART.length);
		System.arraycopy(AUTH_SUCCESS_PATTERNS, 0, INFO_PATTERNS, INFO_PATTERNS_PART.length, AUTH_SUCCESS_PATTERNS.length);
		INFO_PATTERNS[INFO_PATTERNS_PART.length + AUTH_SUCCESS_PATTERNS.length] = SESS_START_PATTERN;

		ALL_PATTERNS = new Pattern[INFO_PATTERNS.length + FAIL_PATTERNS.length + FAIL_PATTERNS_DUP.length];
		System.arraycopy(INFO_PATTERNS, 0, ALL_PATTERNS, 0, INFO_PATTERNS.length);
		System.arraycopy(FAIL_PATTERNS, 0, ALL_PATTERNS, INFO_PATTERNS.length, FAIL_PATTERNS.length);
		System.arraycopy(FAIL_PATTERNS_DUP, 0, ALL_PATTERNS, INFO_PATTERNS.length + FAIL_PATTERNS.length, FAIL_PATTERNS_DUP.length);
	}

	protected AbstractChecker() {
	}

	// ---- 実装クラスに対するサービスメソッド

	// 引数のリストからIsp情報を取得する
	protected IspList getIsp(NetAddr addr, MagList maglist, KnownList knownlist) {
		IspList isp = null;
		if (addr == null) {
			return null;
		}

		if (maglist != null) {
			isp = maglist.get(addr);
			if (isp != null) {
				return isp;
			}
		}
		if (knownlist != null) {
			isp = knownlist.get(addr);
			// knownlist.get(...)はnullを返却しない
			return isp;
		}
		// Whoisクラスではサイト情報が取得できない場合でも、
		// 必ずクラスを生成するので取得できない場合はあり得ない。はず
		addrErrs.add(addr);
		return null;
	}

	// ---- 実装クラスの制御メソッド
	private T run(String[] files) throws InterruptedException {
		log.log(Level.INFO, "checking from files={0}:", files);

		List<String> f = Arrays.asList(files);
		List<InputStream> list = f.stream()
				.map(t -> {
					try {
						return new FileInputStream(t);
					}
					catch (FileNotFoundException ex) {
						log.log(Level.WARNING, ex.getMessage());
					}
					return null;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		Enumeration<InputStream> e = new Enumeration<InputStream>() {
			Iterator<InputStream> i = list.iterator();
			@Override
			public boolean hasMoreElements() {
				return i.hasNext();
			}
			@Override
			public InputStream nextElement() {
				return i.next();
			}
		};
		SequenceInputStream sis = new SequenceInputStream(e);
		return run(sis);
	}
	@WithElaps
	private T run(InputStream is) throws InterruptedException {
		log.info("checking from InputStream:");

		Stream<String> stream = new BufferedReader(new InputStreamReader(is)).lines();
		ExecutorService exec = Executors.newSingleThreadExecutor();
		PrintDot dot = new PrintDot();
		try {
			// PrintDotスレッドの実行
			// Executor.execute(): Runnableスレッド
			exec.execute(dot);

			// Checkerクラスの実行
			return call(stream);
		}
		finally {
			// PrintDotスレッドの停止要求
			dot.stopRequest();
			// PrintDotスレッドの終了の待ち合わせ
			while (exec.awaitTermination(1, TimeUnit.SECONDS)) {
				// Do nothing
			}
		}
	}

	// checkerスレッドの実行メソッド
	protected abstract T call(Stream<String> stream);
	protected abstract void report(final PrintWriter out, final T map);

	// サブクラス外からの呼び出しを考慮してpublicとする
	@WithElaps
	public int start(String[] argv, int offset)
			throws InterruptedException, ExecutionException, IOException
	{
		int rc = 0;
		T map = null;
		if (argv.length <= offset) {
			map = run(System.in);
		}
		else {
			String[] newargv = Arrays.copyOfRange(argv, offset, argv.length);
			map = run(newargv);
		}

		addrErrs.forEach(addr -> log.log(Level.WARNING, "unknown ip: addr={0}", addr));
		userErrs.forEach(userId -> log.log(Level.WARNING, "not found user: userid={0}", userId));
		projErrs.forEach(proj -> log.log(Level.WARNING, "unknown proj: proj={0}", proj));
		ptnErrs.forEach(ptn -> log.log(Level.WARNING, "unknown patten: pattern={0}", ptn));

		// 結果の出力:
		// Excelで読めるCSV形式とするためUnixでも改行コードを"\r\n"に変更する
		String crlf = System.getProperty(LINE_SEPARATOR);
		System.setProperty(LINE_SEPARATOR, "\r\n");
		PrintWriter out = new PrintWriter(System.out);
		report(out, map);
		out.flush();
		System.setProperty(LINE_SEPARATOR, crlf);

		return rc;
	}

	/*
	 * 進捗状態を示すために1秒間隔で"."を出力するためのクラス
	 */
	private class PrintDot implements Runnable {

		private final PrintStream err;
		private boolean stopRequest = false;

		private PrintDot() {
			this.err = System.err;
		}

		public void run() {
			while (!stopRequest) {
				try {
					Thread.sleep(1000);
					err.print(".");
				} catch (InterruptedException e) {
					// もし例外が発生してしまうとスレッドが停止してしまうが
					// sonarのパーサが文句を言うので仕方がない
					Thread.currentThread().interrupt();
				}
			}
			err.println();
		}

		public void stopRequest() {
			stopRequest = true;
		}
	}

}
