package logcheck.util;

public class NetAddr implements Comparable<NetAddr> {

	private final int[] srcaddr;
	private final int[] netaddr;
	private final int[] brdaddr;
	private final int mask;

	public NetAddr(String addr) {
		srcaddr = new int[4];
		netaddr = new int[4];
		brdaddr = new int[4];

		String[] s0 = addr.split("/");
		if (s0.length != 1 && s0.length != 2) {
			throw new IllegalArgumentException("netaddr error: " + addr);
		}
	
		String[] s1 = s0[0].split("\\.");
		if (s1.length != 4) {
			throw new IllegalArgumentException("ip error: " + s0[0] + ", len=" + s1.length);
		}

		for (int ix = 0; ix < 4; ix++) {
			srcaddr[ix] = Integer.parseInt(s1[ix]);
			netaddr[ix] = srcaddr[ix];
			brdaddr[ix] = srcaddr[ix];
		}

		//mask = s0.length == 1 ? 32 : Byte.parseByte(s0[1]);
		if (s0.length < 2) {
			mask = 32;
		}
		else {
			String[] s2 = s0[1].split("\\.");
			if (s2.length == 1) {
				mask = Byte.parseByte(s0[1]);
			}
			else if (s2.length == 4) {
				int tmp_mask = 0;
				for (int ix = 0; ix < 4; ix++) {
					int m = Integer.parseInt(s2[ix]);
					while (m > 0) {
						m = m / 2;
						++tmp_mask;
					}
				}
				mask = tmp_mask;
			}
			else {
				throw new IllegalArgumentException("ip error: " + s0[0] + ", len=" + s1.length);
			}
		}

		if (mask > 0 && mask <= 8) {
			netaddr[0] = netaddr[0] & (int)(256 - Math.pow(2, 8 - mask));
			netaddr[1] = 0;
			netaddr[2] = 0;
			netaddr[3] = 0;
			brdaddr[0] = brdaddr[0] | (int)(Math.pow(2, 8 - mask) - 1);
			brdaddr[1] = 255;
			brdaddr[2] = 255;
			brdaddr[3] = 255;
		}
		else if (mask > 8 && mask <= 16) {
			netaddr[1] = netaddr[1] & (int)(256 - Math.pow(2, 16 - mask));
			netaddr[2] = 0;
			netaddr[3] = 0;
			brdaddr[1] = brdaddr[1] | (int)(Math.pow(2, 16 - mask) - 1);
			brdaddr[2] = 255;
			brdaddr[3] = 255;
		}
		else if (mask > 16 && mask <= 24) {
			netaddr[2] = netaddr[2] & (int)(256 - Math.pow(2, 24 - mask));
			netaddr[3] = 0;
			brdaddr[2] = brdaddr[2] | (int)(Math.pow(2, 24 - mask) - 1);
			brdaddr[3] = 255;
		}
		else {
			netaddr[3] = netaddr[3] & (int)(256 - Math.pow(2, 32 - mask));
			brdaddr[3] = brdaddr[3] | (int)(Math.pow(2, 32 - mask) - 1);
		}
		//System.out.println("NetworkAddr<init>: addr=" + addr + ", this=" + this);
	}

	public int compareTo(NetAddr another) {
		int[] addr = another.getNetworkAddr();
		for (int ix = 0; ix < 4; ix++) {
			if (netaddr[ix] - addr[ix] != 0) {
				return netaddr[ix] - addr[ix];
			}
		}
		addr = another.getBroadcastAddr();
		for (int ix = 0; ix < 4; ix++) {
			if (brdaddr[ix] - addr[ix] != 0) {
				return brdaddr[ix] - addr[ix];
			}
		}
		return 0;
	}
	public boolean equals(NetAddr another) {
		return compareTo(another) == 0 ? true : false;
	}
	/*
	public int hashCode() {
		long hash = 0;
		for (int ix = 0; ix < 4; ix++) {
			hash = hash * 256 + netaddr[ix];
		}
		return (int)hash;
	}
	*/
	public boolean within(NetAddr another) {
		int[] addr = another.getNetworkAddr();
		for (int ix = 0; ix < 4; ix++) {
			if (addr[ix] == netaddr[ix]) { }
			else if (addr[ix] < netaddr[ix]) {
				return false;
			}
		}
		addr = another.getBroadcastAddr();
		for (int ix = 0; ix < 4; ix++) {
			if (addr[ix] == brdaddr[ix]) { }
			if (addr[ix] > brdaddr[ix]) {
				return false;
			}
		}
		return true;
	}

	public int[] getNetworkAddr() {
		return netaddr;
	}
	public int[] getBroadcastAddr() {
		return brdaddr;
	}
	public int getMask() {
		return mask;
	}
	public String toStringRange() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(netaddr[0]).append(".").append(netaddr[1]).append(".").append(netaddr[2]).append(".").append(netaddr[3]);
		sb.append("-");
		sb.append(brdaddr[0]).append(".").append(brdaddr[1]).append(".").append(brdaddr[2]).append(".").append(brdaddr[3]);
		sb.append(")");
		return sb.toString();
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(srcaddr[0]).append(".").append(srcaddr[1]).append(".").append(srcaddr[2]).append(".").append(srcaddr[3]);
		sb.append("/").append(mask);
		return sb.toString();
	}

	public static void main(String...argv) {
		/*
		System.out.println(new NetAddr("192.168.15.15"));
		System.out.println(new NetAddr("192.168.15.15/30"));
		System.out.println(new NetAddr("192.168.15.15/28"));
		System.out.println(new NetAddr("192.168.15.15/24"));
		System.out.println(new NetAddr("192.168.15.15/22"));
		System.out.println(new NetAddr("192.168.15.15/20"));
		System.out.println(new NetAddr("172.130.1.1/18"));
		System.out.println(new NetAddr("172.130.1.1/16"));
		System.out.println(new NetAddr("172.130.1.1/14"));
		System.out.println(new NetAddr("172.130.1.1/12"));
		System.out.println(new NetAddr("172.130.1.1/10"));
		System.out.println(new NetAddr("172.130.1.1/8"));
		System.out.println(new NetAddr("10.10.1.1/8"));

		System.out.println(new NetAddr("192.168.15.10/30").within(new NetAddr("192.168.15.13")));
		System.out.println(new NetAddr("192.168.15.13/30").within(new NetAddr("192.168.15.13")));
		System.out.println(new NetAddr("192.168.15.16/30").within(new NetAddr("192.168.15.13")));
		*/
		System.out.println(new NetAddr("163.135.0.0/16").within(new NetAddr("163.135.151.75")));

		//		NetworkAddr na1 = new NetworkAddr("192.168.15.13/30");
//		NetworkAddr na2 = new NetworkAddr("192.168.15.13");
/*
		System.out.println("2 ^ 0 = " + Math.pow(2, 0));
		System.out.println("2 ^ 1 = " + Math.pow(2, 1));
		System.out.println("2 ^ 2 = " + Math.pow(2, 2));
		System.out.println("2 ^ 3 = " + Math.pow(2, 3));
		System.out.println("2 ^ 4 = " + Math.pow(2, 4));
		*/
	}
}
