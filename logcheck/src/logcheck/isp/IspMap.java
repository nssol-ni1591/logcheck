package logcheck.isp;

import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;

import logcheck.util.net.NetAddr;

public class IspMap<V> extends IspBean<TreeMap<NetAddr, V>> {

//	public IspMap(String name) {
//		super(name, "", new TreeMap<NetAddr, V>());
//	}
	public IspMap(String name, String country) {
		super(name, country, new TreeMap<NetAddr, V>());
	}

	public V get(NetAddr addr) {
		return getRef().get(addr);
	}
	public void put(NetAddr addr, V value) {
		getRef().put(addr, value);
	}
	public Set<NetAddr> keySet() {
		return getRef().keySet();
	}
	public Collection<V> values() {
		return getRef().values();
	}

}
