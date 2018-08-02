package logcheck.known.impl;

import logcheck.known.KnownListIsp;
import logcheck.util.NetAddr;

public interface Whois {

	KnownListIsp get(NetAddr addr);

	// for envoronment not using weld-se
	void init();

	// for Callable<KnownListIsp> implement (Yet!)
	void setAddr(NetAddr addr);

	String getName();

}
