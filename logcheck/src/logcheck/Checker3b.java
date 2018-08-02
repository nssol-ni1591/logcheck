package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import logcheck.isp.IspList;
import logcheck.isp.IspMap;
import logcheck.known.KnownList;
import logcheck.log.AccessLog;
import logcheck.log.AccessLogBean;
import logcheck.mag.MagList;
import logcheck.util.NetAddr;
import logcheck.util.WeldWrapper;

/*
 * ISP > IPアドレス > メッセージ毎にログ数を集計する
 */
public class Checker3b extends AbstractChecker<Map<String, IspMap<Map<String, Integer>>>> {

	@Inject private Logger log;

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	public void init(String...argv) throws IOException, ClassNotFoundException, SQLException {
		this.knownlist.load(argv[0]);
		this.maglist.load(argv[1]);
	}

	@Override
	public Map<String, IspMap<Map<String, Integer>>> call(Stream<String> stream) {
		return 
			stream.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.collect(Collectors.groupingBy(
						b -> {
							NetAddr addr = b.getAddr();
							IspList isp = getIsp(addr, maglist, knownlist);
							return isp.getName();
						}
						, new Collector<AccessLogBean, IspMap<Map<String, Integer>>, IspMap<Map<String, Integer>>>() {

							@Override
							public Supplier<IspMap<Map<String, Integer>>> supplier() {
								return IspMap::new;
							}

							@Override
							public BiConsumer<IspMap<Map<String, Integer>>, AccessLogBean> accumulator() {
								return (t, b) -> {
									// t: キー(NetAddr)が一致するIspMap or supplier()で生成された中身が空のIspMap
									// b: AccessLogBean

									//System.out.println("t=[" + t.getCountry() + "," + t.getName() + "], b=" + b)
									// => このメソッドが呼び出された時点でキーが判明している仕組みがわからない
									// => groupingBy()の第一引数でキーを取得している

									Optional<String> rc = Stream.of(FAIL_PATTERNS_ALL)
											.filter(p -> p.matcher(b.getMsg()).matches())
											.map(Pattern::toString)
											.findFirst();
									String m = rc.isPresent() ? rc.get() : b.getMsg();

									NetAddr addr = b.getAddr();
									IspList isp = getIsp(addr, maglist, knownlist);
									t.setName(isp.getName());
									t.setCountry(isp.getCountry());

									Map<String, Integer> client = t.computeIfAbsent(addr, key -> new TreeMap<>());
									Integer count = client.computeIfAbsent(m, key -> Integer.valueOf(0));
									count += 1;
									client.put(m, count);
								};
							}

							@Override
							public BinaryOperator<IspMap<Map<String, Integer>>> combiner() {
								return (t, ispmap) -> {
									// 使用されないらしい ... A, Tが等しいからか？
									log.log(Level.SEVERE, "called combiner");
									return null;
								};
							}

							@Override
							public Function<IspMap<Map<String, Integer>>, IspMap<Map<String, Integer>>> finisher() {
								// => 起動時に1回だけ呼び出される
								log.log(Level.SEVERE, "called finisher");
								return t -> t;
							}

							@Override
							public Set<Characteristics> characteristics() {
								// 多分、使用されない
								return new TreeSet<>();
							}
						}
				));
	}

	/*
	 * ISP > IPアドレス > メッセージ毎に出力する
	 */
	@Override
	public void report(final PrintWriter out, final Map<String, IspMap<Map<String, Integer>>> map) {
		map.values().forEach(isp -> {
			out.println();
			out.println(isp.getName() + (isp.getCountry() == null ? "" : " (" + isp.getCountry() + ")") + " : ");
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
		int rc = new WeldWrapper(Checker3b.class).weld(2, argv);
		System.exit(rc);
	}

}
