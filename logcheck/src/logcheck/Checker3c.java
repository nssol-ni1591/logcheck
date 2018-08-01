package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;
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
import logcheck.util.net.NetAddr;
import logcheck.util.weld.WeldWrapper;

/*
 * ISP > IPアドレス > メッセージ毎にログ数を集計する
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
			stream//.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.map(LogWrapper::new)
				.collect(Collectors.groupingBy(
						LogWrapper::getKey
						, Collectors.groupingBy(
								// IspMap2にnameとcountryを設定するタイミングがない
								// => keyに"name (country)"を設定する
								/*
								new Function<LogWrapper, NetAddr>() {
									@Override
									public NetAddr apply(LogWrapper t) {
										return t.getAddr();
									}
								}
								*/
								LogWrapper::getAddr
								/*
								, new Supplier<IspMap2<Map<String, Integer>>>() {
									@Override
									public IspMap2<Map<String, Integer>> get() {
										return new IspMap2<>();
									}
								}
								*/
								, IspMap2::new		// 指定しないとHashMap
								, Collectors.toMap(
										//Collector<T, ?, Map<K,U>> toMap
										// 第1引数：キーを取得するための処理
										//  new Function<? super T, ? extends K>() { public K apply(T t) { return null; } }
										// 第2引数：取得したキーに対する値がない時の処理
										//, new Function<? super T, ? extends U>() { public U apply(T t) { return null; } }
										// 第3引数：取得したキーに対する値が存在するときの値のマージ
										//, new BinaryOperator<U>() { public U apply(U t, U u) { return null; } }
										// ※第3引数を省略した場合：keyが重複するとき"IllegalStateException: Duplicate key ??"が発生するので
										/*
										new Function<LogWrapper, String>() {
											@Override
											public String apply(LogWrapper t) {
												return t.getPattern();
											}
										}
										*/
										//log -> log.getPattern()
										LogWrapper::getPattern
										/*
										, new Function<LogWrapper, Integer>() {
											@Override
											public Integer apply(LogWrapper log) {
												return Integer.valueOf(1);
											}
										}
										*/
										, log -> Integer.valueOf(1)
										/*
										, new BinaryOperator<Integer>() {
											@Override
											public Integer apply(Integer t, Integer u) {
												return t + u;
											}
										}
										*/
										/*
										, (e1, e2) -> {
											return e1 + e2;
										}
										*/
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
		//map.values().forEach(isp -> {
		map.forEach((name, isp) -> {
			out.println();
			out.println(name + (isp.getCountry() == null ? "" : " (" + isp.getCountry() + ")") + " : ");
			isp.keySet().forEach(addr -> {
				Map<String, Integer> msgs = isp.get(addr);
				out.println("\t" + addr + " : ");
				msgs.keySet().forEach(msg -> 
					out.println("\t\t[ " + msg + " ] : " + msgs.get(msg))
				);
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
			pattern = rc.isPresent() ? rc.get() : b.getMsg();
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
