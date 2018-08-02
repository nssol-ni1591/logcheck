package logcheck.isp;

import java.util.Objects;
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
		if (country == null) {
			this.country = null;
		}
		else {
			this.country = country.length() != 2 ? country : country.toUpperCase();
		}
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
		if (country == null) {
			this.country = null;
		}
		else {
			this.country = country.length() != 2 ? country : country.toUpperCase();
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		Objects.requireNonNull(o);

		if (o instanceof IspMap2) {
			IspMap2<?> map = (IspMap2<?>)o;
			if (!Objects.equals(country, map.getCountry())) {
				return false;
			}
			return Objects.equals(name, map.getName());
		}
		return false;
	}
}
