package logcheck;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import logcheck.log.AccessLog;
import logcheck.log.AccessLogBean;
import logcheck.log.AccessLogSummary;
import logcheck.util.net.NetAddr;
import logcheck.util.weld.WeldWrapper;

/*
 * ログ解析用の集約ツール1''： Checker8、22の簡略版
 * 国 > ISP > クライアントIP > メッセージ  > ID 毎にログ数を集計する。
 * なお、このツールでは、正常系ログは集約を行わず、ispのグループ化も行わない。
 * つまり、メッセージのパターンチェックが目的のプログラム。
 */
public class Checker21 extends AbstractChecker<Map<NetAddr, Map<String, Map<String, AccessLogSummary>>>> {

	@Inject private Logger log;

	public void init(String...argv) {
		// Do nothing
	}

	// ログのメッセージ部分はPatternの正規化表現で集約するため、対象ログが一致したPattern文字列を取得する
	protected String getPattern(AccessLogBean b) {
		Optional<String> rc = Stream.of(ALL_PATTERNS)
				.filter(p -> p.matcher(b.getMsg()).matches())
				.map(Pattern::toString)
				.findFirst();
		if (rc.isPresent()) {
			return rc.get();
		}
		log.warning("(Pattern): \"" + b.getMsg() + "\"");
		return b.getMsg();
	}

	@Override
	public Map<NetAddr, Map<String, Map<String, AccessLogSummary>>> call(Stream<String> stream) {
		final Map<NetAddr, Map<String, Map<String, AccessLogSummary>>> map = new TreeMap<>();
		stream//.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.forEach(b -> {
					String pattern = getPattern(b);
					NetAddr addr = b.getAddr();

					Map<String, Map<String, AccessLogSummary>> idmap;
					Map<String, AccessLogSummary> msgmap;
					AccessLogSummary msg;

					idmap = map.get(addr);
					if (idmap == null) {
						idmap = new TreeMap<>();
						map.put(addr, idmap);
					}

					msgmap = idmap.get(b.getId());
					if (msgmap == null) {
						msgmap = new TreeMap<>();
						idmap.put(b.getId(), msgmap);
					}

					msg = msgmap.get(pattern);
					if (msg == null) {
						msg = new AccessLogSummary(b, pattern);
						msgmap.put(pattern, msg);
					} else {
						msg.update(b);
					}

				});
		return map;
	}

	@Override
	public void report(final PrintWriter out, 
			final Map<NetAddr, Map<String, Map<String, AccessLogSummary>>> map)
	{
		out.println("アドレス\tユーザID\tメッセージ\tロール\t初回日時\t最終日時\tログ数");
		map.forEach((addr, idmap) -> 
			idmap.forEach((id, msgmap) -> 
				msgmap.forEach((pattern, msg) -> 
					Stream.of(msg.getRoles()).forEach(role -> 
						out.println(Stream.of(addr.toString()
								, id
								, pattern
								, role
								, msg.getFirstDate()
								, msg.getLastDate()
								, String.valueOf(msg.getCount())	//　rolesの出力数倍になる
								)
								.collect(Collectors.joining("\t")))
							)
						)
					)
				);
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper<Checker21>(Checker21.class).weld(2, argv);
		System.exit(rc);
	}
}
