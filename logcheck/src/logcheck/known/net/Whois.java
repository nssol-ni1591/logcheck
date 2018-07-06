package logcheck.known.net;

import logcheck.known.KnownListIsp;
import logcheck.util.net.NetAddr;

public interface Whois {

	KnownListIsp get(NetAddr addr);

	// for envoronment not using weld-se
	void init();

	// for Callable<KnownListIsp> implement (Yet!)
	void setAddr(NetAddr addr);

	String getName();

}
