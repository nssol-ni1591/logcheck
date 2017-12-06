package logcheck.known.tsv;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class TsvKnownListBean {

	@Inject private Logger log;

	private final String addr;
	private final String name;
	private final String country;

	public TsvKnownListBean(String addr, String name, String country) {
		this.addr = addr;
		this.name = name;
		this.country = country;
	}

	@PostConstruct
	public void init() {
		log.log(Level.FINE, "TsvKnownListBean={0}", toString());
	}
	public String getAddr() {
		return addr;
	}
	public String getName() {
		return name;
	}
	public String getCountry() {
		return country;
	}

	@Override
	public String toString() {
		return String.format("addr=%s, name=%s, country=%s", addr, name, country);
	}
}
