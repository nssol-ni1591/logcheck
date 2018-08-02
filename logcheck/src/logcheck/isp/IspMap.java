package logcheck.isp;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

import logcheck.util.NetAddr;

public class IspMap<V> implements Isp {

	private String name;
	private String country;
	private Map<NetAddr, V> ref;

	public IspMap() {
		this.ref = new TreeMap<>();
	}
	public IspMap(String name, String country) {
		this.name = name;
		this.country = country;
		this.ref = new TreeMap<>();
	}

	private Map<NetAddr, V> getRef() {
		return ref;
	}
	
	public V get(NetAddr addr) {
		return getRef().get(addr);
	}
	public void put(NetAddr addr, V value) {
		getRef().put(addr, value);
	}
	// For Checker3[ab]...
	public Set<NetAddr> keySet() {
		return getRef().keySet();
	}
	// For Checker4...
	public Collection<V> values() {
		return getRef().values();
	}

	public V computeIfAbsent(NetAddr key,
			Function<? super NetAddr, ? extends V> mappingFunction) {
		return getRef().computeIfAbsent(key, mappingFunction);
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
