package logcheck;

import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.log.AccessLogBean;
import logcheck.log.AccessLogSummary;
import logcheck.mag.MagList;
import logcheck.util.NetAddr;

/*
 * ユーザ認証ログ突合せ処理：
 * ユーザ認証の成功ログと同じアドレス、同一ユーザIDの認証失敗ログを検索する
 */
public class Checker10 extends AbstractChecker<List<AccessLogSummary>> /*implements Predicate<AccessLogBean>*/ {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	private static final Pattern[] AUTH_PATTERNS = {
			Pattern.compile("Primary authentication successful for [\\S ]+ from [\\d\\.]+"),
//			Pattern.compile("Primary authentication failed for [\\S ]+ from \\S+"),
			Pattern.compile("Login failed using auth server NSSDC_LDAP \\(LDAP Server\\).  Reason: Failed"),
			Pattern.compile("Login failed using auth server NSSDC_LDAP \\(LDAP Server\\).  Reason: Short Password"),
	};

	public Checker10 init(String knownfile, String magfile) throws Exception {
		this.knownlist.load(knownfile);
		this.maglist.load(magfile);
		return this;
	}

	public static boolean test(AccessLogBean b) {
		// メッセージにIPアドレスなどが含まれるログは、それ以外の部分を比較対象とするための前処理
		return Stream.of(AUTH_PATTERNS)
				.filter(p -> p.matcher(b.getMsg()).matches())
				.map(p -> p.toString())
				.findFirst()
				.isPresent();
	}

	public List<AccessLogSummary> call(Stream<String> stream) throws IOException {
		List<AccessLogSummary> list = new Vector<>(1000);
		stream//.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.filter(Checker10::test)
				.forEach(b -> {
					NetAddr addr = b.getAddr();
					IspList isp = maglist.get(addr);
					if (isp == null) {
						isp = knownlist.get(addr);
					}

					if (isp != null) {
						AccessLogSummary msg = null;
						if (b.getMsg().contains("failed")) {
							// 失敗メッセージ
							if (list.isEmpty()) {
								msg = new AccessLogSummary(b, b.getMsg(), isp);
								list.add(msg);
							}
							else {
								String date = b.getDate().substring(0, 10);
								for (int ix = list.size() - 1; ix >= 0; ix--) {
									msg = list.get(ix);
									if (!msg.getFirstDate().startsWith(date)) {
										msg = null;
										break;
									}
									else if (msg.getAddr().equals(b.getAddr()) && msg.getId().equals(b.getId())) {
										msg.addCount();
										break;
									}
									msg = null;
								}
								if (msg == null) {
									msg = new AccessLogSummary(b, b.getMsg(), isp);
									list.add(msg);
								}
							}
						}
						else {
							// 成功メッセージ
							String date = b.getDate().substring(0, 10);
							for (int ix = list.size() - 1; ix >= 0; ix--) {
								msg = list.get(ix);
								if (!msg.getFirstDate().startsWith(date)) {
									break;
								}
								else if (msg.getAddr().equals(b.getAddr()) && msg.getId().equals(b.getId())) {
									list.remove(ix);
									break;
								}
							}
						}

					}
					else {
//						System.err.println("unknown ip: addr=" + addr);
						log.warning("unknown ip: addr=" + addr);
					}
				});
		return list;
	}

	public void report(List<AccessLogSummary> list) {
		System.out.println("出力日時\t国\tISP/プロジェクト\tアドレス\tユーザID\tエラー回数\tメッセージ");

		list.forEach(msg -> {
			System.out.println(
					new StringBuilder(msg.getFirstDate())
					.append("\t")
					.append(msg.getIsp().getCountry())
					.append("\t")
					.append(msg.getIsp().getName())
					.append("\t")
					.append(msg.getAddr())
					.append("\t")
					.append(msg.getId())
					.append("\t")
					.append(msg.getCount())
					.append("\t")
					.append(msg.getPattern())
					);
		});
	}

	public static void main(String... argv) {
		if (argv.length < 3) {
			System.err.println("usage: java logcheck.Checker10 knownlist maglist [accesslog...]");
			System.exit(1);
		}

		int rc = 0;
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			Checker10 application = container.instance().select(Checker10.class).get();
			application.init(argv[0], argv[1]).start(argv, 2);
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
			rc = 1;
		}
		System.exit(rc);
	}
}
