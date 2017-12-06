package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.inject.Inject;

import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.log.AccessLogSummary;
import logcheck.mag.MagList;
import logcheck.util.net.NetAddr;
import logcheck.util.weld.WeldWrapper;

/*
 * ユーザ認証ログ突合せ処理：
 * まず、VPNログを読み込み、ユーザ認証に失敗したログの場合は、[ユーザ認証失敗コレクション]を検索し、
 * IPアドレスとユーザIDがも等しいログがあったならば、ログ回数を加算する。
 * 該当するログが存在しない場合は、[ユーザ認証失敗コレクション]にログを登録する。
 * もし、ログがユーザ認証の成功ログの場合は、成功ログのIPアドレスとユーザIDが等しいログを、
 * [ユーザ認証失敗コレクション]を検索し、存在していた場合はコレクションのエントリを削除する。
 */
public class Checker10 extends AbstractChecker<List<AccessLogSummary>> /*implements Predicate<AccessLogBean>*/ {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	private static final Pattern[] AUTH_PATTERNS = {
			Pattern.compile("Primary authentication successful for [\\S ]+ from [\\d\\.]+"),
			Pattern.compile("Login failed using auth server (NSSDC_LDAP|SDC-AD) \\([\\w ]+\\).  Reason: Failed"),
			Pattern.compile("Login failed using auth server (NSSDC_LDAP|SDC-AD) \\([\\w ]+\\).  Reason: Short Password"),
	};

	public void init(String...argv) throws Exception {
		this.knownlist.load(argv[0]);
		this.maglist.load(argv[1]);
	}

	@Override
	public List<AccessLogSummary> call(Stream<String> stream) throws IOException {
		final List<AccessLogSummary> list = new Vector<>(1000);
		stream//.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.filter(b -> Stream.of(AUTH_PATTERNS)
						// 正規化表現に一致するメッセージのみを処理対象にする
						.anyMatch(p -> p.matcher(b.getMsg()).matches())
						)
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
							// listをさかのぼる範囲は同じ日付のログまで
							String date = b.getDate().substring(0, 10);
							for (int ix = list.size() - 1; ix >= 0; ix--) {
								msg = list.get(ix);
								if (!msg.getFirstDate().startsWith(date)) {
									// listの日付が変わったのでさかのぼる処理をやめる
									break;
								}

								if (!"".equals(msg.getReason()) && !msg.getReason().endsWith("（※）：")) {
									// すでに"（※）"で終わらない原因が設定されている場合は置換を行わない
								}
								else if (msg.getAddr().equals(b.getAddr()) && msg.getId().equals(b.getId())) {
									// アドレスもユーザIDも一致している場合
									msg.setReason("パスワードの入力ミス：");
									msg.setDetail(b.getDate() + " に認証成功");
								}
								else if (msg.getId().equals(b.getId())) {
									// アドレスが一致していないが、ユーザIDが一致している場合
									msg.setReason("VPN利用方法のミス：");
									msg.setDetail(b.getAddr() + " からの認証成功");
								}
								else if ("利用申請".equals(msg.getIsp().getCountry())) {
									msg.setReason("利用申請先からの接続：");
									msg.setDetail("問題なしとする");
								}
								else if (msg.getAddr().equals(b.getAddr())) {
									// アドレスが一致しているが、ユーザIDが一致していない場合
									msg.setAfterUsrId(b.getId());
									msg.setReason("ユーザIDの入力ミス（※）：");
									msg.setDetail(b.getId() + " / " + b.getDate() + " での認証成功");
								}
							}
						}
					}
					else {
						addrErrs.add(b.getAddr());
					}
				});
		list.stream()
			.filter(sum -> "".equals(sum.getReason()))
			.forEach(sum -> {
				if (sum.getCount() <= 10) {
					sum.setReason("経過観察（※）：");
				}
				else {
					sum.setReason("ログ精査（※）：");
				}
			});
		return list;
	}

	@Override
	public void report(final PrintWriter out, final List<AccessLogSummary> list) {
		out.println("出力日時\t国\tISP/プロジェクト\tアドレス\tユーザID\t参考ユーザID\tエラー回数\t想定される原因\t詳細");
		list.forEach(msg -> 
			out.println(new StringBuilder(msg.getFirstDate())
					.append("\t").append(msg.getIsp().getCountry())
					.append("\t").append(msg.getIsp().getName())
					.append("\t").append(msg.getAddr())
					.append("\t").append(msg.getId())
					.append("\t").append(msg.getAfterUsrId())
					.append("\t").append(msg.getCount())
					.append("\t").append(msg.getReason())
					.append("\t").append(msg.getDetail())
					)
		);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper<Checker10>(Checker10.class).weld(2, argv);
		System.exit(rc);
	}
}
