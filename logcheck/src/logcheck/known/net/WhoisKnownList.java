package logcheck.known.net;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import logcheck.known.KnownList;
import logcheck.known.KnownListIsp;
import logcheck.known.net.apnic.WhoisApnic;
import logcheck.known.net.arin.WhoisArin;
import logcheck.known.net.html.WhoisLacnic;
import logcheck.known.net.html.WhoisTreetCoJp;
import logcheck.known.net.jpnic.WhoisJpnic;
import logcheck.known.tsv.TsvKnownList;
import logcheck.util.Constants;
import logcheck.util.net.NetAddr;

@Alternative
public class WhoisKnownList extends LinkedHashSet<KnownListIsp> implements KnownList {

	@Inject private Logger log;
	private static final long serialVersionUID = 1L;
	
	@Inject private WhoisTreetCoJp treet;
	@Inject private WhoisLacnic lacnic;
	@Inject private WhoisApnic apnic;
	@Inject private WhoisJpnic jpnic;
	@Inject private WhoisArin arin;

	// KnownList
	@Override
	public void init() {
		// No nothing
	}

	private boolean check(KnownListIsp isp) {
		if (isp == null) {
			return false;
		}

		if (isp.getName() == null) {
			return false;
		}
		if (isp.getCountry() == null) {
			return false;
		}
		return !isp.getAddress().isEmpty();
	}
	/*
	 * JPNICから引数のIPアドレスを含むISPを取得する
	 */
	private KnownListIsp getJpnic(NetAddr addr, KnownListIsp isp) {
		boolean rc = false;
		if (isp != null) {
			// ただし、特定のISP名の場合、遅いJPNICを検索しても同じ結果になるので、再検索をskipする
			rc = WhoisUtils.isSkipJpnic(isp.getName());
		}
		if (!rc) {
			KnownListIsp tmp = jpnic.get(addr);
			if (check(tmp)) {
				log.log(Level.INFO, "{0}: addr={1} => name={2}, country={3}, net={4}",
						new Object[] { jpnic.getName(), addr, tmp.getName(), tmp.getCountry(), tmp.toStringNetwork() });
				return tmp;
			}
		}
		return isp;
	}
	/*
	 * 正常なISP情報が取得できない場合、
	 * 一時保管したmapのサイト情報から属性ごとに値を取得しサイト情報の組み立てを行う
	 */
	private KnownListIsp buildIsp(NetAddr addr, Map<Whois, KnownListIsp> map) {
		String name = null;
		String country = null;

		// get country
		Optional<KnownListIsp> rc4 = map.values().stream()
				.filter(Objects::nonNull)
				.filter(i -> i.getCountry() != null)
				.findFirst();
		if (rc4.isPresent()) {
			country = rc4.get().getCountry();
		}
		else {
			country = Constants.UNKNOWN_COUNTRY;
		}

		// get name
		Optional<KnownListIsp> rc3 = map.values().stream()
				.filter(Objects::nonNull)
				.filter(i -> i.getName() != null)
				.filter(i -> !i.getName().isEmpty())
				.findFirst();
		if (rc3.isPresent()) {
			name = rc3.get().getName();

			final KnownListIsp isp2 = new KnownListIsp(name, country);
			// nameが取得できたサイトのアドレスをコピーする
			rc3.get().getAddress().forEach(isp2::addAddress);
			return isp2;
		}
		else {
			// nameの取得ができない場合は、エラー識別のためにnameに検索したアドレスを設定する
			name = addr.toString();
			return new KnownListIsp(name, country);
		}
	}
	/*
	 * 引数のIPアドレスを含むISPを取得する
	 */
	@Override
	public KnownListIsp get(NetAddr addr) {
		Optional<KnownListIsp> rc = this.stream()
				.filter(isp -> isp.within(addr))
				.findFirst();
		if (rc.isPresent()) {
			// Hit cache
			return rc.get();
		}

		// { apnic, treet, lacnic, arin, jpnic }から選択
		// 注意：jpnicはレスポンスが遅いので最後
		// 注意：apnicはレスポンスが遅くなってきた ... why?
		final Whois[] whois = { treet, arin, apnic, lacnic };

		// check()結果がfalseの場合、取得したISP情報を一時的に保持するための領域
		final Map<Whois, KnownListIsp> map = new LinkedHashMap<>();

		KnownListIsp isp = null;
		Optional<Whois> rc2 = Arrays.stream(whois)
				.filter(w -> {
					// WhoisサーバからISP情報を取得する
					KnownListIsp tmp = w.get(addr);
					map.put(w, tmp);
					if (tmp == null) {
						log.log(Level.INFO, "{0}: addr={1} => isp={2}", new Object[] { w.getName(), addr, tmp });
						return false;
					}
					log.log(Level.INFO, "{0}: addr={1} => name={2}, country={3}, net={4}",
							new Object[] { w.getName(), addr, tmp.getName(), tmp.getCountry(), tmp.toStringNetwork() });
					return check(tmp);
				})
				.findFirst();

		if (rc2.isPresent()) {
			// サイト情報を正常に取得できた場合：
			Whois w = rc2.get();
			isp = map.get(w);
		}
		
		// ISPの取得に失敗した場合、もしくは、日本のISPの場合はJPNICでの再検索を行う
		if (isp == null || isp.getCountry() == null || "JP".equals(isp.getCountry())) {
			isp = getJpnic(addr, isp);
		}

		// 正常なISP情報が取得できなかった場合の処理
		if (!check(isp)) {
			isp = buildIsp(addr, map);
		}

		// ISP情報を登録する
		add(isp);
		return isp;
	}

	@Override
	public KnownList load(String file) throws IOException {
		KnownList list = new TsvKnownList();
		list.init();
		list.load(file);
		list.forEach(this::add);
		return this;
	}

	@Override
	public void store(String file) throws IOException {
		try (PrintWriter out = new PrintWriter(file, "MS932")) {
			this.forEach(isp ->
				isp.getAddress().forEach(addr ->
					out.printf("%s\t%s\t%s%s", addr.toStringNetwork(), isp.getName(), isp.getCountry(), System.lineSeparator())
				)
			);
			out.flush();
		}
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
