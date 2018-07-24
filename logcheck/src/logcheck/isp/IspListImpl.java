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

	@Override
	public int compareTo(IspList o) {
		if (o == null) {
			return -1;
		}
		if (getCountry() == null) {
			if (o.getCountry() != null) {
				return 1;
			}
		}
		else {
			int rc = getCountry().compareTo(o.getCountry());
			if (rc != 0) {
				return rc;
			}
		}
		if (getName() == null) {
			if (o.getName() != null) {
				return 1;
			}
		}
		else {
			int rc = getName().compareTo(o.getName());
			if (rc != 0) {
				return rc;
			}
		}
		return 0;
	}
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o instanceof IspList) {
			return compareTo((IspList)o) == 0;
		}
		return false;
	}

}
