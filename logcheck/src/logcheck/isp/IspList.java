package logcheck.isp;

import java.util.Set;

import logcheck.util.NetAddr;

public interface IspList extends Isp {

	Set<NetAddr> getAddress();

	void addAddress(NetAddr addr);

	default boolean within(NetAddr addr) {
		return getAddress().stream().anyMatch(net -> net.within(addr));
	}

}
