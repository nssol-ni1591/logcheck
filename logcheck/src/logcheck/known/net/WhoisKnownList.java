package logcheck.known.net;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
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
public class WhoisKnownList extends LinkedHashSet<KnownListIsp> implements KnownList, Whois {

	@Inject private transient Logger log;
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	@Inject private WhoisTreetCoJp treet;
	@SuppressWarnings("unused")
	@Inject private WhoisLacnic lacnic;
	@Inject private WhoisApnic apnic;
	@Inject private WhoisJpnic jpnic;
	@Inject private WhoisArin arin;

	@Override
	public void init() {
		// No nothing
	}

	private boolean check(KnownListIsp isp) {
		if (isp.getName() == null) {
			return false;
		}
		if (isp.getCountry() == null) {
			return false;
		}
		return !isp.getAddress().isEmpty();
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
		final Whois[] whois = { arin, apnic, jpnic };

		// check()結果がfalseの場合、取得したISP情報を一時的に保持するための領域
		final Map<Whois, KnownListIsp> map = new HashMap<>();

		KnownListIsp isp = null;
		Optional<Whois> rc2 = Arrays.stream(whois)
				.filter(w -> {
					// WhoisサーバからISP情報を取得する
					KnownListIsp tmp = w.get(addr);
					map.put(w, tmp);
					if (tmp == null) {
						log.log(Level.INFO, "{0}: addr={1} => isp={2}", new Object[] { w.getClass().getSimpleName(), addr, tmp });
						return false;
					}
					log.log(Level.INFO, "{0}: addr={1} => name={2}, country={3}, net={4}",
							new Object[] { w.getClass().getSimpleName(), addr, tmp.getName(), tmp.getCountry(), tmp.toStringNetwork() });
					return check(tmp);
				})
				.findFirst();

		if (rc2.isPresent()) {
			// サイト情報を正常に取得できた場合：
			Whois w = rc2.get();
			isp = map.get(w);
		}
		else {
			//　サイト情報の取得に失敗した場合：
			// 一時保管したmapの情報からサイト情報の組み立てを行う
			// networkアドレスはなしで、Pivotテーブルのグルーピングキーとして使用する
			String name = null;
			String country = null;

			// get name
			Optional<Whois> rc3 = Arrays.stream(whois)
					.filter(w -> map.get(w) != null)
					.filter(w -> map.get(w).getName() != null)
					.filter(w -> !map.get(w).getName().isEmpty())
					.findFirst();
			if (rc3.isPresent()) {
				name = map.get(rc3.get()).getName();
			}
			// get country
			Optional<Whois> rc4 = Arrays.stream(whois)
					.filter(w -> map.get(w) != null)
					.filter(w -> map.get(w).getCountry() != null)
					.filter(w -> !map.get(w).getCountry().isEmpty())
					.findFirst();
			if (rc4.isPresent()) {
				country = map.get(rc4.get()).getCountry();
			}

			if (country == null) {
				country = Constants.UNKNOWN_COUNTRY;
			}

			if (name != null) {
				final KnownListIsp isp2 = new KnownListIsp(name, country);
				// nameが取得できたサイトのアドレスをコピーする
				map.get(rc3.get()).getAddress().forEach(isp2::addAddress);
				isp = isp2;
			}
			else {
				// nameの取得ができない場合は、エラー識別のためにnameに検索したアドレスを設定する
				name = addr.toString();
				isp = new KnownListIsp(name, country);
			}
		}

		if (isp != null) {
			add(isp);
		}
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
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

}
