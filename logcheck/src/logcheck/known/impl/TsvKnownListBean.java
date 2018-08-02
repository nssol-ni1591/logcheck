package logcheck.known.impl;


public class TsvKnownListBean {

	private final String addr;
	private final String name;
	private final String country;

	public TsvKnownListBean(String addr, String name, String country) {
		this.addr = addr;
		this.name = name;
		this.country = country;
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

}
