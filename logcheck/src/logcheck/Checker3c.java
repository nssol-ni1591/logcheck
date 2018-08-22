package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import logcheck.isp.IspList;
import logcheck.isp.IspMap2;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.log.AccessLogBean;
import logcheck.mag.MagList;
import logcheck.util.NetAddr;
import logcheck.util.WeldWrapper;

/*
 * ISP > IPアドレス > メッセージ毎にログ数を集計する
 * 
 * Checker3bと比較して
 * (1)groupingByを見やすくするためにLogWrapperを追加した
 * (2)lambda式を使う
 * (3)groupingBy()ではMapに属性を持たせる仕組みはないのでMapのkeyに属性を持たせた
 * 
 * ちなみに、実現したかったこと：　IspMap2用のCollectorを用意したい
 * (1)下段のCollectorとのつなぎは同実装すればよいのか
 * (2)可能ならば、IspMap2の属性を設定するにはどうすればよいのか？
 * 
 * => Collector.of(...)が提供するのは、終端のtoMap()とかtoList()に対するもので、
 * 中間のgroupingByに対するものではない。ということで諦めた
 */
public class Checker3c extends AbstractChecker<Map<String, IspMap2<Map<String, Integer>>>> {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	public void init(String...argv) throws IOException, ClassNotFoundException, SQLException {
		this.knownlist.load(argv[0]);
		this.maglist.load(argv[1]);
	}

	@Override
	public Map<String, IspMap2<Map<String, Integer>>> call(Stream<String> stream) {
		return 
			stream.parallel()
				//.filter(AccessLog::test)
				.map(AccessLog::parse)
				.filter(Objects::nonNull)
				.map(LogWrapper::new)
				.filter(log -> log.getPattern() != null)
				.collect(Collectors.groupingBy(
						LogWrapper::getKey
						, Collectors.groupingBy(
								// IspMap2にnameとcountryを設定するタイミングがない
								// => keyに"name (country)"を設定する
								LogWrapper::getAddr
								, IspMap2::new		// 指定しないとHashMap
								, Collectors.toMap(
										//Collector<T, ?, Map<K,U>> toMap
										// 第1引数：キーを取得するための処理
										//  new Function<? super T, ? extends K>() { public K apply(T t) { return null; } } -
										// 第2引数：取得したキーに対する値がない時の処理
										//, new Function<? super T, ? extends U>() { public U apply(T t) { return null; } } -
										// 第3引数：取得したキーに対する値が存在するときの値のマージ
										//, new BinaryOperator<U>() { public U apply(U t, U u) { return null; } } -
										// ※第3引数を省略した場合：keyが重複するとき"IllegalStateException: Duplicate key ??"が発生するので
										LogWrapper::getPattern
										, log -> Integer.valueOf(1)
										, (e1, e2) -> e1 + e2
										, TreeMap::new		// 指定しないとHashMap
										)
								)
						));
	}

	/*
	 * ISP > IPアドレス > メッセージ毎に出力する
	 */
	@Override
	public void report(final PrintWriter out, final Map<String, IspMap2<Map<String, Integer>>> map) {
		map.forEach((key, isp) -> {
			out.println();
			out.println(key);
			isp.keySet().forEach(addr -> {
				Map<String, Integer> msgs = isp.get(addr);
				out.println("\t" + addr + " : ");
				msgs.keySet().forEach(msg -> out.println("\t\t[ " + msg + " ] : " + msgs.get(msg)));
			});
		});
		out.println();
	}

	public static void main(String ... argv) {
		int rc = new WeldWrapper(Checker3c.class).weld(2, argv);
		System.exit(rc);
	}

	class LogWrapper {

		private final NetAddr addr;
		private final IspList isp;
		private final String pattern;
		private final String key;

		public LogWrapper(AccessLogBean b) {
			this.addr = b.getAddr();
			this.isp = getIsp(addr, maglist, knownlist);
			this.key = isp.getName() + " (" + isp.getCountry() + ")";
			Optional<String> rc = Stream.of(FAIL_PATTERNS_ALL)
					.filter(p -> p.matcher(b.getMsg()).matches())
					.map(Pattern::toString)
					.findFirst();
			pattern = rc.isPresent() ? rc.get() : null;
		}

		public NetAddr getAddr() {
			return addr;
		}
		public String getKey() {
			return key;
		}
		public String getPattern() {
			return pattern;
		}

	}

}
