package logcheck.util.net;

public class ClientAddr extends NetAddr {

	public ClientAddr(String addr) {
		super(addr);
		// TODO Auto-generated constructor stub
	}

	public String toString() {
		int[] srcaddr = getAddr();
		StringBuilder sb = new StringBuilder();
		sb.append(srcaddr[0]).append(".").append(srcaddr[1]).append(".").append(srcaddr[2]).append(".").append(srcaddr[3]);
		return sb.toString();
	}

}
