package logcheck.isp;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import logcheck.util.net.NetAddr;

public class IspListImpl extends IspBean<Set<NetAddr>> implements IspList {

	public IspListImpl(String name, String country) {
		super(name, country, new TreeSet<NetAddr>());
	}

	public Set<NetAddr> getAddress() {
		return getRef();
	}
	public void addAddress(NetAddr addr) {
		getRef().add(addr);
	}

	public Set<String> toStringNetwork() {
		Set<String> set = new HashSet<>();
		getAddress().forEach(addr -> set.add(addr.toStringNetwork()));
		return set;
	}

}
