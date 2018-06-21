package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.inject.Inject;

import logcheck.log.AccessLog;
import logcheck.proj.ProjList;
import logcheck.proj.ProjListBean;
import logcheck.util.weld.WeldWrapper;

/*
 * プロジェクトの利用状況を取得する：
 * 
 * 
 */
public class Checker19 extends AbstractChecker<ProjList<ProjListBean>> {

	@Inject private Logger log;
	@Inject protected ProjList<ProjListBean> projlist;

	public void init(String...argv) throws IOException, ClassNotFoundException, SQLException {
		this.projlist.load();
	}

	@Override
	public ProjList<ProjListBean> call(Stream<String> stream) {
		stream//.parallel()		// parallel()を使用するとOutOfMemory例外が発生する　=> なぜ?
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.filter(b -> SESS_START_PATTERN.matcher(b.getMsg()).matches())
				.forEach(b -> {
					String[] roles = b.getRoles();

					for (String projId : roles) {
						ProjListBean proj = projlist.get(projId);
						if (proj == null) {
							projErrs.add(projId);

							// ログに存在するがリストに存在しない場合： 不正な状態を検知することができるようにuserlistに追加する
							proj = new ProjListBean(projId, "-1");
							projlist.put(projId, proj);
						}

						proj.update(b, SESS_START_PATTERN.toString());
						log.config(String.format("proj=%s, user=%s", projId, b.getId()));
					}
				});
		return projlist;
	}

	@Override
	public void report(final PrintWriter out, final ProjList<ProjListBean> list) {
		// アドレスを出力してはいけない。拠点ごとに回数を取得しているのに、アドレスを出力すると、回数は実際の値のアドレス数の倍になる
		out.println("プロジェクトID\tプロジェクト削除\tユーザID\t初回日時\t最終日時\t接続回数");
		projlist.values().stream()
			.forEach(proj -> {
				if (proj.getLogs().isEmpty()) {
					out.println(new StringBuilder(proj.getProjId())
							.append("\t").append(proj.getValidFlag())
							.append("\t").append("-")
							.append("\t").append("-")
							.append("\t").append("-")
							.append("\t").append("0")
							);
				}
				else {
					proj.getLogs().values().forEach(sum ->
						out.println(new StringBuilder(proj.getProjId())
								.append("\t").append(proj.getValidFlag())
								.append("\t").append(sum.getId())
								.append("\t").append(sum.getFirstDate())
								.append("\t").append(sum.getLastDate())
								.append("\t").append(sum.getCount())
								)
					);
				}
			});
	}

	@Override
	public String usage(String name) {
		return String.format("usage: java %s [accesslog...]", name);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper<Checker19>(Checker19.class).weld(2, argv);
		System.exit(rc);
	}
}
