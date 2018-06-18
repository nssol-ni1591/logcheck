package logcheck.known.net;

import logcheck.known.KnownListIsp;
import logcheck.util.net.NetAddr;

public interface Whois {

	KnownListIsp get(NetAddr addr);

}
