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
					/*
					while (m > 0) {
						m = m / 2;
						++tmp_mask;
					}
					*/
					switch (m) {
					case 0:		tmp_mask += 0 ; break;
					case 127:	tmp_mask += 1 ; break;
					case 128:	tmp_mask += 1 ; break;	// 記入ミス対応
					case 192:	tmp_mask += 2 ; break;
					case 224:	tmp_mask += 3 ; break;
					case 240:	tmp_mask += 4 ; break;
					case 248:	tmp_mask += 5 ; break;
					case 252:	tmp_mask += 6 ; break;
					case 254:	tmp_mask += 7 ; break;
					case 255:	tmp_mask += 8 ; break;
					default:
						throw new IllegalArgumentException("ip error: " + addr + ", s2=" + s2);
					}
				}
				mask = tmp_mask;
			}
			else {
				throw new IllegalArgumentException("ip error: " + addr + ", len=" + s1.length);
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

	public int[] getAddr() {
		return srcaddr;
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

}
