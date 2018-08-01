package logcheck.isp;

import java.util.TreeMap;

import logcheck.util.net.NetAddr;

public class IspMap2<V> extends TreeMap<NetAddr, V> implements Isp {

	private static final long serialVersionUID = 1L;

	private String name;
	private String country;

	public IspMap2() {
		// Do nothing
	}
	public IspMap2(String name, String country) {
		this.name = name;
		this.country = country;
	}
		
	@Override
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}

}
