package logcheck.isp;

import java.util.Set;
import java.util.TreeSet;

import logcheck.util.NetAddr;

public class IspList extends IspBean<Set<NetAddr>> implements Isp {

	public IspList(String name, String country) {
		super(name, country, new TreeSet<NetAddr>());
	}

	public Set<NetAddr> getAddress() {
		return getRef();
	}
	public void addAddress(NetAddr addr) {
		getRef().add(addr);
	}
}
