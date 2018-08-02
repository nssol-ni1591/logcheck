package logcheck;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Collector.Characteristics;

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
public class Checker3e extends AbstractChecker<Map<String, IspMap2<Map<String, Integer>>>> {

	@Inject private KnownList knownlist;
	@Inject private MagList maglist;

	public void init(String...argv) throws IOException, ClassNotFoundException, SQLException {
		this.knownlist.load(argv[0]);
		this.maglist.load(argv[1]);
	}

	private static Collector<LogWrapper, ?, IspMap2<Map<String, Integer>>> c =
			Collectors.groupingBy(
					LogWrapper::getAddr
					, IspMap2::new
					, Collectors.toMap(
							LogWrapper::getPattern
							, log -> Integer.valueOf(1)
							, (e1, e2) -> e1 + e2
							, TreeMap::new));			// 指定しないとHashMap
	private static Collector<LogWrapper, IspMap2<Map<String, Integer>>, IspMap2<Map<String, Integer>>> c1 =
			Collector.of(
					/*
					new Supplier<IspMap2<Map<String, Integer>>>() {
						@Override
						public IspMap2<Map<String, Integer>> get() {
							return new IspMap2<>();
						}
					}
					*/
					IspMap2::new
					/*
					, new BiConsumer<IspMap2<Map<String, Integer>>, LogWrapper>() {
						public void accept(IspMap2<Map<String, Integer>> t, LogWrapper u) {
							t.setName(u.getName());
							t.setCountry(u.getCountry());
						}
					}
					*/
					, (t, u) -> {
						t.setName(u.getName());
						t.setCountry(u.getCountry());
					}
					/*
					, new BinaryOperator<IspMap2<Map<String, Integer>>>() {
						public IspMap2<Map<String, Integer>> apply(IspMap2<Map<String, Integer>> t, IspMap2<Map<String, Integer>> u) {
							// 使用されないらしい ... A, Tが等しいからか？
							System.out.println("called combiner");
							return null;
						}
					}
					*/
					, (t, u) -> t
					/*
					, new Function<IspMap2<Map<String, Integer>>, IspMap2<Map<String, Integer>>>() {
						public IspMap2<Map<String, Integer>> apply(IspMap2<Map<String, Integer>> t) {
							//System.out.println("called finisher")
							// => 起動時に1回だけ呼び出される
							return t;
						}
					}
					*/
					, t -> t
					, Characteristics.IDENTITY_FINISH
					);

	@Override
	public Map<String, IspMap2<Map<String, Integer>>> call(Stream<String> stream) {
		return 
			stream.parallel()
				.filter(AccessLog::test)
				.map(AccessLog::parse)
				.map(LogWrapper::new)
				.collect(Collectors.groupingBy(
						LogWrapper::getName
						//, new Collector<LogWrapper, IspMap2<Map<String, Integer>>, IspMap2<Map<String, Integer>>>() { }
						//, c
						, c1
						));
	}

	/*
	 * ISP > IPアドレス > メッセージ毎に出力する
	 */
	@Override
	public void report(final PrintWriter out, final Map<String, IspMap2<Map<String, Integer>>> map) {
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
		int rc = new WeldWrapper(Checker3e.class).weld(2, argv);
		System.exit(rc);
	}

	class LogWrapper {

		private final NetAddr addr;
		private final IspList isp;
		private final String pattern;

		private String name;
		private String country;

		public LogWrapper(AccessLogBean b) {
			this.addr = b.getAddr();
			this.isp = getIsp(addr, maglist, knownlist);
			this.name = isp.getName();
			this.country = isp.getCountry();
			Optional<String> rc = Stream.of(FAIL_PATTERNS_ALL)
					.filter(p -> p.matcher(b.getMsg()).matches())
					.map(Pattern::toString)
					.findFirst();
			pattern = rc.isPresent() ? rc.get() : b.getMsg();
		}

		public NetAddr getAddr() {
			return addr;
		}
		/*
		public NetAddr getAddr(LogWrapper log) {
			this.name = log.getName();
			this.country = log.getCountry();
			return addr;
		}
		*/
		/*
		public IspList getIspList() {
			return isp;
		}
		*/
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getCountry() {
			return country;
		}
		public void setCountry(String country) {
			this.country = country;
		}
		public String getPattern() {
			return pattern;
		}
		/*
		public IspMap2<Map<String, Integer>> createIspMap2() {
			return new IspMap2<>(name, country);
		}
		public IspMap2<Map<String, Integer>> createIspMap2(LogWrapper log) {
			return new IspMap2<>(log.getName(), log.getCountry());
		}
		*/
	}
}
