package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.inject.Inject;

import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.log.AccessLogBean;
import logcheck.log.AccessLogSummary;
import logcheck.mag.MagList;
import logcheck.util.NetAddr;
import logcheck.util.WeldWrapper;

/*
 * ユーザ認証ログ突合せ処理：
 * まず、VPNログを読み込み、ユーザ認証に失敗したログの場合は、[ユーザ認証失敗コレクション]を検索し、
 * IPアドレスとユーザIDがも等しいログがあったならば、ログ回数を加算する。
 * 該当するログが存在しない場合は、[ユーザ認証失敗コレクション]にログを登録する。
 * もし、ログがユーザ認証の成功ログの場合は、成功ログのIPアドレスとユーザIDが等しいログを、
 * [ユーザ認証失敗コレクション]を検索し、存在していた場合はコレクションのエントリを削除する。
 */
public class Checker10 extends AbstractChecker<List<AccessLogSummary>> {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	protected static final Pattern[] AUTH_PATTERNS;
	static {
		AUTH_PATTERNS = new Pattern[AUTH_SUCCESS_PATTERNS.length + AUTH_FAILED_PATTERNS.length];
		System.arraycopy(AUTH_SUCCESS_PATTERNS, 0, AUTH_PATTERNS, 0, AUTH_SUCCESS_PATTERNS.length);
		System.arraycopy(AUTH_FAILED_PATTERNS, 0, AUTH_PATTERNS, AUTH_SUCCESS_PATTERNS.length, AUTH_FAILED_PATTERNS.length);
	}

	public void init(String...argv) throws IOException, ClassNotFoundException, SQLException {
		this.knownlist.load(argv[0]);
		this.maglist.load(argv[1]);
	}

	// 失敗メッセージの場合：
	private void failed(List<AccessLogSummary> list, IspList isp, AccessLogBean b) {
		AccessLogSummary msg = null;

		if (list.isEmpty()) {
			msg = new AccessLogSummary(b, b.getMsg(), isp);
			list.add(msg);
			return;
		}

		// 過去のfailedリストに(日付、クライアントアドレス)が一致するログを検索する
		String date = b.getDate().substring(0, 10);	// ログの発生日の取得
		for (int ix = list.size() - 1; ix >= 0; ix--) {
			msg = list.get(ix);
			if (!msg.getFirstDate().startsWith(date)) {
				// ログの日付が変わった場合は検索を中止し、空のAccessLogSummaryを追加する
				break;
			}
			if (msg.getAddr().equals(b.getAddr()) && msg.getId().equals(b.getId())) {
				// listに同じクライアントアドレスのログが登録されていた場合はカウントを更新する
				msg.addCount();
				return;
			}
		}
		// 同一の日時andクライアントアドレのログが存在しない場合は、空のAccessLogSummaryを追加する
		msg = new AccessLogSummary(b, b.getMsg(), isp);
		list.add(msg);
	}

	// 成功メッセージの場合：
	private void success(List<AccessLogSummary> list, AccessLogBean b) {
		AccessLogSummary msg = null;

		// failedリストから(日付、アドレス、ユーザ)が一致するAccessLogSummaryを検索する
		// ⇒登録済の場合：何もしない
		// ⇒存在しない場合：一致する条件によってログイン失敗の原因を決定する
		String date = b.getDate().substring(0, 10);	// ログの発生日の取得
		for (int ix = list.size() - 1; ix >= 0; ix--) {
			msg = list.get(ix);
			if (!msg.getFirstDate().startsWith(date)) {
				// listの日付が変わったのでさかのぼる処理をやめる
				return;
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

	@Override
	public List<AccessLogSummary> call(Stream<String> stream) {
		final List<AccessLogSummary> list = new ArrayList<>();
		stream//.parallel()
				.map(AccessLog::parse)
				.filter(Objects::nonNull)
				.filter(b -> Stream.of(AUTH_PATTERNS)
						// 正規化表現に一致するメッセージのみを処理対象にする
						.anyMatch(p -> p.matcher(b.getMsg()).matches())
						)
				.forEach(b -> {
					if (b.getMsg().contains("failed")) {
						// 失敗メッセージ：
						// Ispの取得は失敗メッセージの場合だけ行えばよい
						NetAddr addr = b.getAddr();
						IspList isp = getIsp(addr, maglist, knownlist);
						if (isp != null) {
							failed(list, isp, b);
						}
					}
					else {
						// 成功メッセージ：
						success(list, b);
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
		out.println(String.join("\t"
				, "出力日時"
				, "国"
				, "ISP/プロジェクト"
				, "アドレス"
				, "ユーザID"
				, "参考ユーザID"
				, "エラー回数"
				, "想定される原因"
				, "詳細"));
		list.forEach(msg -> 
			out.println(String.join("\t"
					, msg.getFirstDate()
					, msg.getIsp().getCountry()
					, msg.getIsp().getName()
					, msg.getAddr().toString()
					, msg.getId()
					, msg.getAfterUsrId()
					, String.valueOf(msg.getCount())
					, msg.getReason()
					, msg.getDetail()
					))
				);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper(Checker10.class).weld(2, argv);
		System.exit(rc);
	}
}
