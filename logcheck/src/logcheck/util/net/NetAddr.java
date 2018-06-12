package logcheck.util.net;

public class NetAddr implements Comparable<NetAddr> {

	private final int[] srcaddr;
	private final int[] network;
	private final int[] brdcast;
	private final int mask;

	public NetAddr(String src, String net, String brd) {
		srcaddr = addr(src);
		network = addr(net);
		brdcast = addr(brd);

		int tmp = 0;
		for (int ix = 0; ix < 4; ix++) {
			switch (brdcast[ix] - network[ix]) {
			case 0:		tmp += 8; break;
			case 1:		tmp += 7; break;
			case 3:		tmp += 6; break;
			case 7:		tmp += 5; break;
			case 15:	tmp += 4; break;
			case 32:	tmp += 3; break;
			case 63:	tmp += 2; break;
			case 127:	tmp += 1; break;
			case 255:	tmp += 0; break;
			default:
				// skip
			}
		}
		this.mask = tmp;
	}
	public NetAddr(String addr) {
		network = new int[4];
		brdcast = new int[4];

		String[] s0 = addr.split("/");
		if (s0.length != 1 && s0.length != 2) {
			throw new IllegalArgumentException("network error: " + addr);
		}

		srcaddr = addr(s0[0]);

		for (int ix = 0; ix < 4; ix++) {
			network[ix] = srcaddr[ix];
			brdcast[ix] = srcaddr[ix];
		}

		if (s0.length < 2) {
			mask = 32;
		}
		else {
			String[] s2 = s0[1].split("\\.");
			if (s2.length == 1) {
				mask = Byte.parseByte(s0[1]);
			}
			else if (s2.length == 4) {
				int tmp = 0;
				for (int ix = 0; ix < 4; ix++) {
					int m = Integer.parseInt(s2[ix]);

					switch (m) {
					case 0:		tmp += 0 ; break;
					case 127:	tmp += 1 ; break;
					case 128:	tmp += 1 ; break;	// 記入ミス対応
					case 192:	tmp += 2 ; break;
					case 224:	tmp += 3 ; break;
					case 240:	tmp += 4 ; break;
					case 248:	tmp += 5 ; break;
					case 252:	tmp += 6 ; break;
					case 254:	tmp += 7 ; break;
					case 255:	tmp += 8 ; break;
					default:
						throw new IllegalArgumentException("ip error(1): " + addr + ", s2=" + s2);
					}
				}
				mask = tmp;
			}
			else {
				throw new IllegalArgumentException("ip error(2): " + addr + ", len=" + s2.length);
			}
		}

		if (mask > 0 && mask <= 8) {
			network[0] = network[0] & (int)(256 - Math.pow(2, (8 - mask)));
			network[1] = 0;
			network[2] = 0;
			network[3] = 0;
			brdcast[0] = brdcast[0] | (int)(Math.pow(2, (8 - mask)) - 1);
			brdcast[1] = 255;
			brdcast[2] = 255;
			brdcast[3] = 255;
		}
		else if (mask > 8 && mask <= 16) {
			network[1] = network[1] & (int)(256 - Math.pow(2, (16 - mask)));
			network[2] = 0;
			network[3] = 0;
			brdcast[1] = brdcast[1] | (int)(Math.pow(2, (16 - mask)) - 1);
			brdcast[2] = 255;
			brdcast[3] = 255;
		}
		else if (mask > 16 && mask <= 24) {
			network[2] = network[2] & (int)(256 - Math.pow(2, (24 - mask)));
			network[3] = 0;
			brdcast[2] = brdcast[2] | (int)(Math.pow(2, (24 - mask)) - 1);
			brdcast[3] = 255;
		}
		else {
			network[3] = network[3] & (int)(256 - Math.pow(2, (32 - mask)));
			brdcast[3] = brdcast[3] | (int)(Math.pow(2, (32 - mask)) - 1);
		}
	}

	private int[] addr(String s) {
		String[] s1 = s.split("\\.");
		if (s1.length != 4) {
			throw new IllegalArgumentException("ip error(3): " + s + ", len=" + s1.length);
		}
		int[] addr = new int[4];
		for (int ix = 0; ix < 4; ix++) {
			addr[ix] = Integer.parseInt(s1[ix]);
		}
		return addr;
	}

	public int compareTo(NetAddr another) {
		int[] addr = another.getNetworkAddr();
		for (int ix = 0; ix < 4; ix++) {
			if (network[ix] - addr[ix] != 0) {
				return network[ix] - addr[ix];
			}
		}
		addr = another.getBroadcastAddr();
		for (int ix = 0; ix < 4; ix++) {
			if (brdcast[ix] - addr[ix] != 0) {
				return brdcast[ix] - addr[ix];
			}
		}
		return 0;
	}
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public boolean equals(Object another) {
		if (another instanceof NetAddr) {
			return compareTo((NetAddr)another) == 0 ? true : false;
		}
		return super.equals(another);
	}

	public boolean within(NetAddr another) {
		int[] addr = another.getNetworkAddr();
		// 多分、0.0.0.0 => 非固定 のチェックは行わなくても大丈夫
		for (int ix = 0; ix < 4; ix++) {
			if (addr[ix] == network[ix]) {
				// 等しい場合は何もしない
			}
			else if (addr[ix] < network[ix]) {
				return false;
			}
		}
		addr = another.getBroadcastAddr();
		for (int ix = 0; ix < 4; ix++) {
			if (addr[ix] == brdcast[ix]) {
				// 等しい場合は何もしない
			}
			if (addr[ix] > brdcast[ix]) {
				return false;
			}
		}
		return true;
	}

	public int[] getAddr() {
		return srcaddr;
	}
	public int[] getNetworkAddr() {
		return network;
	}
	public int[] getBroadcastAddr() {
		return brdcast;
	}
	public int getMask() {
		return mask;
	}
	public String toStringRange() {
		StringBuilder sb = new StringBuilder(toString());
		sb.append(" (");
		sb.append(network[0]).append(".").append(network[1]).append(".").append(network[2]).append(".").append(network[3]);
		sb.append("-");
		sb.append(brdcast[0]).append(".").append(brdcast[1]).append(".").append(brdcast[2]).append(".").append(brdcast[3]);
		sb.append(")");
		return sb.toString();
	}
	public String toStringNetwork() {
		StringBuilder sb = new StringBuilder();
		sb.append(network[0]).append(".").append(network[1]).append(".").append(network[2]).append(".").append(network[3]);
		sb.append("/").append(mask);
		return sb.toString();
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(srcaddr[0]).append(".").append(srcaddr[1]).append(".").append(srcaddr[2]).append(".").append(srcaddr[3]);
		//sb.append("/").append(mask);
		return sb.toString();
	}

}
