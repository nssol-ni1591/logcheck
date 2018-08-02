package logcheck.util.net;

public class ClientAddr extends NetAddr {

	public ClientAddr(String addr) {
		super(addr);
	}

	@Override
	public String toString() {
		return toIpaddr(getAddr());
	}

}
