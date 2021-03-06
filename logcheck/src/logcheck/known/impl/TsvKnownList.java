package logcheck.known.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import logcheck.annotations.WithElaps;
import logcheck.known.KnownList;
import logcheck.known.KnownListIsp;
import logcheck.util.Constants;
import logcheck.util.NetAddr;

/*
 * 既知のISPのIPアドレスを取得する
 * 取得先は、引数に指定された「既知ISP_IPアドレス一覧」ファイル
 * 
 * 問題点：
 * 広いアドレス空間をISPが取得し、その一部を企業に貸し出しているよう場合、
 * IPアドレスから取得される接続元はISP名ではなく企業名を取得したい。
 * 今のHashMapでは、Hash地の値により、どちらが取得されるか判断付かない。
 */
@Alternative
public class TsvKnownList extends LinkedHashSet<KnownListIsp> implements KnownList {

	@Inject private Logger log;

	private static final long serialVersionUID = 1L;

	public static final String PATTERN = "(\\d+\\.\\d+\\.\\d+\\.\\d+/?\\d*)\t([^\t]+)\t(プライベート|\\S\\S)";

	public TsvKnownList() {
		super(200);
	}

	@Override
	public void init() {
		if (log == null) {
			// JUnitの場合、logのインスタンスが生成できないため
			log = Logger.getLogger(this.getClass().getName());
		}
	}

	/*
	 * 引数のIPアドレスを含むISPを取得する
	 */
	public KnownListIsp get(NetAddr addr) {
		Optional<KnownListIsp> rc = this.stream()
				.filter(isp -> isp.within(addr))
				.findFirst();
		return rc.isPresent() ? rc.get() : new KnownListIsp(addr.toString(), Constants.UNKNOWN_COUNTRY);
	}

	@WithElaps
	public KnownList load(String file) throws IOException {
		log.log(Level.INFO, "start load ... file={0}", (file == null ? "null" : file));
		try (Stream<String> input = Files.lines(Paths.get(file), Charset.forName("MS932"))) {
			input//.filter(TsvKnownList::test)
				.map(TsvKnownList::parse)
				.filter(Objects::nonNull)
				.forEach(b -> {
					Optional<KnownListIsp> rc = this.stream()
							.filter(i -> i.within(new NetAddr(b.getAddr())))
							.findFirst();
					if (rc.isPresent()) {
						KnownListIsp isp = rc.get();
						isp.addAddress(new NetAddr(b.getAddr()));
					}
					else {
						KnownListIsp isp = new KnownListIsp(b.getName(), b.getCountry());
						add(isp);
						isp.addAddress(new NetAddr(b.getAddr()));
					}
				});
		}
		log.log(Level.INFO, "end load ... size={0}", this.size());
		return this;
	}

	public static TsvKnownListBean parse(String s) {
		if (s.startsWith("#")) {
			return null;
		}

		Pattern p = Pattern.compile(PATTERN);
		Matcher m = p.matcher(s);
		if (!m.find()) {
			Logger.getLogger(TsvKnownList.class.getName()).log(Level.WARNING, "(既知ISP_IPアドレス): s=\"{0}\"", s);
			return null;
		}

		String addr = m.group(1);
		String name = m.group(2);
		String country = m.group(3);

		if (name.length() > 0 && name.charAt(0) == '\"') {
			name = name.substring(1);
		}
		if (name.length() > 0 && name.charAt(name.length() - 1) == '\"') {
			name = name.substring(0, name.length() - 1);
		}
		return new TsvKnownListBean(addr, name, country);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

}
