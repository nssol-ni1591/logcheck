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
import logcheck.util.net.NetAddr;

@Alternative
public class WhoisKnownList extends LinkedHashSet<KnownListIsp> implements KnownList {

	@Inject private Logger log;
	private static final long serialVersionUID = 1L;
	
	@Inject WhoisTreetCoJp treet;
	@Inject WhoisLacnic lacnic;
	@Inject WhoisApnic apnic;
	@Inject WhoisJpnic jpnic;
	@Inject WhoisArin arin;

	public void init() {
		if (log == null) {
			// logのインスタンスが生成できないため
			log = Logger.getLogger(this.getClass().getName());
		}
		if (treet == null) {
			treet = new WhoisTreetCoJp();
			treet.init();
		}
		if (lacnic == null) {
			lacnic = new WhoisLacnic();
			lacnic.init();
		}
		if (apnic == null) {
			apnic = new WhoisApnic();
			apnic.init();
		}
		if (jpnic == null) {
			jpnic = new WhoisJpnic();
			jpnic.init();
		}
		if (arin == null) {
			arin = new WhoisArin();
			arin.init();
		}
	}

	private boolean check(KnownListIsp isp) {
		if (isp == null) {
			return false;
		}
		if (isp.getName() == null) {
			return false;
		}
		if (isp.getCountry() == null || isp.getCountry().equals("--")) {
			return false;
		}
		if (isp.getAddress().isEmpty()) {
			return false;
		}
		return true;
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
			return rc.get();
		}

//		final Whois[] whois = { apnic, treet, lacnic, arin, jpnic };
		// jpnicはレスポンスが遅いので最後
		final Whois[] whois = { apnic, arin, jpnic };

		KnownListIsp isp = null;
		Map<Whois, KnownListIsp> map = new HashMap<>();
		Optional<Whois> rc2 = Arrays.stream(whois)
			.filter(w -> {
				KnownListIsp i = w.get(addr);
				map.put(w, i);
				if (i == null) {
					log.log(Level.INFO, "{0}: addr={1} => isp={2}", new Object[] { w.getClass().getSimpleName(), addr, i });
				}
				else {
					log.log(Level.INFO, "{0}: addr={1} => name={2}, country={3}, net={4}",
							new Object[] { w.getClass().getSimpleName(), addr, i.getName(), i.getCountry(), i.toStringNetwork() });
				}
				return check(i);
			})
			.findFirst();

		if (rc2.isPresent()) {
			Whois w = rc2.get();
			isp = map.get(w);
		}
		else {
			String name = null;
			String country = null;

			Optional<Whois> rc3 = Arrays.stream(whois)
					.filter(w -> map.get(w) != null)
					.filter(w -> map.get(w).getName() != null)
					.filter(w -> !map.get(w).getName().isEmpty())
					.findFirst();
			if (rc3.isPresent()) {
				name = map.get(rc3.get()).getName();
				country = map.get(rc3.get()).getCountry();

				final KnownListIsp isp2 = new KnownListIsp(name, country);
				map.values().forEach(i -> {
					i.getAddress().forEach(isp2::addAddress);
				});
				isp = isp2;
			}
			else {
				name = addr.toString();
				country = "--";
			}

		}

		if (isp != null) {
			add(isp);
		}
		return isp;
	}

	@Override
	public KnownList load(String file) throws IOException {
		KnownList list = new TsvKnownList().load(file);
		list.forEach(this::add);
		return this;
	}

}
