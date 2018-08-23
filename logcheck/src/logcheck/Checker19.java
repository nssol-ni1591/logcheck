package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import logcheck.log.AccessLog;
import logcheck.proj.ProjList;
import logcheck.proj.ProjListBean;
import logcheck.util.WeldWrapper;

/*
 * プロジェクトの利用状況を取得する：
 * 
 * 
 */
public class Checker19 extends AbstractChecker<ProjList<ProjListBean>> {

	@Inject protected ProjList<ProjListBean> projlist;

	public void init(String...argv) throws IOException, ClassNotFoundException, SQLException {
		this.projlist.load();
	}

	@Override
	public ProjList<ProjListBean> call(Stream<String> stream) {
		stream//.parallel()		// parallel()を使用するとOutOfMemory例外が発生する　=> なぜ?
				.map(AccessLog::parse)
				.filter(Objects::nonNull)
				.filter(b -> SESS_START_PATTERN.matcher(b.getMsg()).matches())
				.forEach(b -> {
					String[] roles = b.getRoles();
					Stream.of(roles)
						.map(projId -> {
							ProjListBean proj = projlist.get(projId);
							if (proj == null) {
								projErrs.add(projId);
								// ログに存在するがリストに存在しない場合⇒不正な状態を検知することができるようにuserlistに追加する
								proj = new ProjListBean(projId, "-1");
								projlist.put(projId, proj);
							}
							return proj;
						})
						.forEach(proj -> {
							proj.update(b, SESS_START_PATTERN.toString());
						});
				});
				//flatMap(b -> Stream.of(b.getRoles()))を使うのも可能だが、update()でAccessLogBeanを渡すことができない
		return projlist;
	}

	@Override
	public void report(final PrintWriter out, final ProjList<ProjListBean> list) {
		// アドレスを出力してはいけない。拠点ごとに回数を取得しているのに、アドレスを出力すると、回数は実際の値のアドレス数の倍になる
		out.println("プロジェクトID\tプロジェクト削除\tユーザID\t初回日時\t最終日時\t接続回数");
		projlist.values().stream()
			.forEach(proj -> {
				if (proj.getLogs().isEmpty()) {
					out.println(Stream.of(proj.getProjId()
							, proj.getValidFlag()
							, "-"
							, "-"
							, "-"
							, "0"
							)
							.collect(Collectors.joining("\t")));
				}
				else {
					proj.getLogs().values().forEach(sum ->
						out.println(Stream.of(proj.getProjId()
								, proj.getValidFlag()
								, sum.getId()
								, sum.getFirstDate()
								, sum.getLastDate()
								, String.valueOf(sum.getCount())
								)
								.collect(Collectors.joining("\t")))
							);
				}
			});
	}

	@Override
	public String usage(String name) {
		return String.format("usage: java %s [accesslog...]", name);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper(Checker19.class).weld(2, argv);
		System.exit(rc);
	}
}
