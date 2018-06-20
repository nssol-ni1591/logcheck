package logcheck.isp;

import java.util.Set;

import logcheck.util.net.NetAddr;

public interface IspList extends Isp {

	Set<NetAddr> getAddress();

	void addAddress(NetAddr addr);
	/*
	default void addAddress(String addr) {
		addAddress(new NetAddr(addr));
	}
	*/
	default boolean within(NetAddr addr) {
		return getAddress().stream().anyMatch(net -> net.within(addr));
	}

}
