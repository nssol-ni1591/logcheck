package logcheck.known;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class KnownListBean {

	private final String addr;
	private final String name;
	private final String country;
	
	@Inject private Logger log;

	public KnownListBean(String addr, String name, String country) {
		this.addr = addr;
		this.name = name;
		this.country = country;
	}

	@PostConstruct
	public void init() {
		log.fine(this.toString());
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

	public String toString() {
		return String.format("addr=%s, name=%s, country=%s", addr, name, country);
	}
}
