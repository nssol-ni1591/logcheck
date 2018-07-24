package logcheck.isp;

import java.util.Set;

import logcheck.util.net.NetAddr;

public interface IspList extends Isp, Comparable<IspList> {

	Set<NetAddr> getAddress();

	void addAddress(NetAddr addr);

	default boolean within(NetAddr addr) {
		return getAddress().stream().anyMatch(net -> net.within(addr));
	}

}
