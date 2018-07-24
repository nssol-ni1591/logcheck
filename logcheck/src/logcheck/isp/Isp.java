package logcheck.isp;

import logcheck.util.net.NetAddr;

public interface Isp {

	String getName();

	String getCountry();

	default String getHostname(NetAddr addr) {
		return addr.toString();
	}

}
