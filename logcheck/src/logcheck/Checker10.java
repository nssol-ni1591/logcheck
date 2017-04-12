package logcheck;

import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import logcheck.isp.IspList;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.log.AccessLogBean;
import logcheck.mag.MagList;
import logcheck.msg.MsgBean;
import logcheck.util.NetAddr;

/*
 * ユーザ認証の失敗後の認証成功ログを確認する
 */
public class Checker10 extends AbstractChecker<List<MsgBean>> /*implements Predicate<AccessLogBean>*/ {

	protected final KnownList knownlist;
	protected final MagList maglist;
	private static final Pattern[] AUTH_PATTERNS = {
			Pattern.compile("Primary authentication successful for [\\S ]+ from [\\d\\.]+"),
			Pattern.compile("Primary authentication failed for [\\S ]+ from \\S+"),
	};
	
	public Checker10(String knownfile, String magfile) throws IOException {
		this.knownlist = loadKnownList(knownfile);
		this.maglist = loadMagList(magfile);
	}

	public static boolean test(AccessLogBean b) {
		// メッセージにIPアドレスなどが含まれるログは、それ以外の部分を比較対象とするための前処理
		return Stream.of(AUTH_PATTERNS)
				.filter(p -> p.matcher(b.getMsg()).matches())
				.map(p -> p.toString())
				.findFirst()
				.isPresent();
	}

	public List<MsgBean> call(Stream<String> stream) throws IOException {
		List<MsgBean> list = new Vector<>(1000);
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
						int backward = list.size() - 10;
						if (backward < 0) {
							backward += 10;
						}

						MsgBean msg = null;
						if (b.getMsg().contains("failed")) {
							// 失敗メッセージ
							if (list.isEmpty()) {
								msg = new MsgBean(b, b.getMsg(), isp);
								list.add(msg);
							}
							else {
								for (int ix = 0; ix < backward; ix++) {
									msg = list.get(list.size() - ix - 1);
									if (msg.getAddr().equals(b.getAddr()) && msg.getIsp().equals(isp) && msg.getId().equals(b.getId())) {
										msg.addCount();
										break;
									}
									msg = null;
								}
								if (msg == null) {
									msg = new MsgBean(b, b.getMsg(), isp);
									list.add(msg);
								}
							}
						}
						else {
							// 成功メッセージ
							for (int ix = 0; ix < backward; ix++) {
								msg = list.get(list.size() - ix - 1);
								if (msg.getAddr().equals(b.getAddr()) && msg.getIsp().equals(isp) && msg.getId().equals(b.getId())) {
									list.remove(list.size() - ix - 1);
									break;
								}
							}
						}

					}
					else {
							System.err.println("unknown ip: addr=" + addr);
					}
				});
		return list;
	}

	public void report(List<MsgBean> list) {
		System.out.println("出力日時\t国\tISP/プロジェクト\tアドレス\tユーザID\tロール\tメッセージ");

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
					.append(msg.getRoles())
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

		try {
			new Checker10(argv[0], argv[1]).start(argv, 2);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
		System.exit(1);
	}
	}
