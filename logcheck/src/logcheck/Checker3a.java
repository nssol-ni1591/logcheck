package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
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
public class Checker3a extends AbstractChecker<Map<String, IspMap<Map<String, Integer>>>> {

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
				//.filter(AccessLog::test)
				.map(AccessLog::parse)
				.filter(Objects::nonNull)
				.collect(Collectors.groupingBy(
						new Function<AccessLogBean, String>() {
							@Override
							public String apply(AccessLogBean b) {
								NetAddr addr = b.getAddr();
								IspList isp = getIsp(addr, maglist, knownlist);
								return isp.getName();
							}
						}
						, new Collector<AccessLogBean, IspMap<Map<String, Integer>>, IspMap<Map<String, Integer>>>() {

							@Override
							public Supplier<IspMap<Map<String, Integer>>> supplier() {
								return new Supplier<IspMap<Map<String, Integer>>>() {
									@Override
									public IspMap<Map<String, Integer>> get() {
										return new IspMap<>();
									}
								};
							}

							@Override
							public BiConsumer<IspMap<Map<String, Integer>>, AccessLogBean> accumulator() {
								return new BiConsumer<IspMap<Map<String, Integer>>, AccessLogBean>() {
									@Override
									public void accept(IspMap<Map<String, Integer>> ispmap, AccessLogBean b) {
										Optional<String> rc = Stream.of(FAIL_PATTERNS_ALL)
												.filter(p -> p.matcher(b.getMsg()).matches())
												.map(Pattern::toString)
												.findFirst();
										if (rc.isPresent()) {
											String m = rc.get();
											NetAddr addr = b.getAddr();
											IspList isp = getIsp(addr, maglist, knownlist);
											ispmap.setName(isp.getName());
											ispmap.setCountry(isp.getCountry());

											Map<String, Integer> client = ispmap.computeIfAbsent(addr, key -> new TreeMap<>());

											Integer count = client.computeIfAbsent(m, key -> Integer.valueOf(0));
											count += 1;
											client.put(m, count);
										}
									}
								};
							}

							@Override
							public BinaryOperator<IspMap<Map<String, Integer>>> combiner() {
								return new BinaryOperator<IspMap<Map<String, Integer>>>() {

									@Override
									public IspMap<Map<String, Integer>> apply(IspMap<Map<String, Integer>> t,
											IspMap<Map<String, Integer>> u) {
										// callされない
										return t;
									}
									
								};
							}

							@Override
							public Function<IspMap<Map<String, Integer>>, IspMap<Map<String, Integer>>> finisher() {
								return new Function<IspMap<Map<String, Integer>>, IspMap<Map<String, Integer>>>() {

									@Override
									public IspMap<Map<String, Integer>> apply(IspMap<Map<String, Integer>> t) {
										return t;
									}
									
								};
							}

							@Override
							public Set<Characteristics> characteristics() {
								// 使用されないので、空のSetを返却する
								return new HashSet<>();
							}
						}
						))
				;
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
		int rc = new WeldWrapper(Checker3a.class).weld(2, argv);
		System.exit(rc);
	}

}
