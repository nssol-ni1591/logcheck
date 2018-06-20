package logcheck.util.net;

public class NetAddr implements Comparable<NetAddr> {

	private final int[] srcaddr;
	private final int[] network;
	private final int[] brdcast;
	private int netmask;

	public NetAddr(String addr, String net, String brd) {
		this.srcaddr = address(addr);
		this.network = address(net);
		this.brdcast = address(brd);
		this.netmask = netmask(network, brdcast);
	}
	public NetAddr(String addr, String netaddr) {
		if (netaddr == null) {
			throw new IllegalArgumentException("network address is null");
		}
		String[] array = netaddr.split("-");
		if (array.length == 2) {
			// "x.x.x.x/x - y.y.y.y.y/y"形式
			if (addr == null) {
				this.srcaddr = address(array[0].trim());
			}
			else {
				this.srcaddr = address(addr);
			}
			this.network = address(array[0].trim());
			this.brdcast = address(array[1].trim());
			this.netmask = netmask(network, brdcast);
		}
		else {
			this.srcaddr = address(addr);
			this.network = new int[4];
			this.brdcast = new int[4];
			calc(netaddr);
		}
	}
	public NetAddr(String netaddr) {
		this(null, netaddr);
	}

	private void calc(String addr) {
		// "xx.xx.xx.xx/xx"形式
		String[] s0 = addr.split("/");
		if (s0.length != 1 && s0.length != 2) {
			throw new IllegalArgumentException("network error: " + addr);
		}

		int[] array = address(s0[0]);
		if (this.srcaddr[0] == 0) {
			for (int ix = 0; ix < 4; ix++) {
				this.srcaddr[ix] = array[ix];
				this.network[ix] = array[ix];
				this.brdcast[ix] = array[ix];
			}
		}
		else {
			for (int ix = 0; ix < 4; ix++) {
				this.network[ix] = array[ix];
				this.brdcast[ix] = array[ix];
			}
		}

		if (s0.length < 2) {
			this.netmask = 32;
		}
		else {
			String[] s2 = s0[1].split("\\.");
			if (s2.length == 1) {
				this.netmask = Byte.parseByte(s0[1]);
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
				this.netmask = tmp;
			}
			else {
				throw new IllegalArgumentException("ip error(2): " + addr + ", len=" + s2.length);
			}
		}

		if (this.netmask > 0 && this.netmask <= 8) {
			this.network[0] = network[0] & (int)(256 - Math.pow(2, (8 - this.netmask)));
			this.network[1] = 0;
			this.network[2] = 0;
			this.network[3] = 0;
			this.brdcast[0] = this.brdcast[0] | (int)(Math.pow(2, (8 - this.netmask)) - 1);
			this.brdcast[1] = 255;
			this.brdcast[2] = 255;
			this.brdcast[3] = 255;
		}
		else if (this.netmask > 8 && this.netmask <= 16) {
			this.network[1] = network[1] & (int)(256 - Math.pow(2, (16 - netmask)));
			this.network[2] = 0;
			this.network[3] = 0;
			this.brdcast[1] = this.brdcast[1] | (int)(Math.pow(2, (16 - this.netmask)) - 1);
			this.brdcast[2] = 255;
			this.brdcast[3] = 255;
		}
		else if (this.netmask > 16 && this.netmask <= 24) {
			this.network[2] = network[2] & (int)(256 - Math.pow(2, (24 - this.netmask)));
			this.network[3] = 0;
			this.brdcast[2] = this.brdcast[2] | (int)(Math.pow(2, (24 - this.netmask)) - 1);
			this.brdcast[3] = 255;
		}
		else {
			this.network[3] = this.network[3] & (int)(256 - Math.pow(2, (32 - this.netmask)));
			this.brdcast[3] = this.brdcast[3] | (int)(Math.pow(2, (32 - this.netmask)) - 1);
		}
	}
	private int netmask(int[] netmask, int[] brdcast) {
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
		return tmp;
	}
	private int[] address(String s) {
		if (s == null) {
			return new int[4];
		}
		String[] s1 = s.split("\\.");
		if (s1.length > 4) {
			throw new IllegalArgumentException("ip error(3): " + s + ", len=" + s1.length);
		}
		int[] addr = new int[4];
		for (int ix = 0; ix < s1.length; ix++) {
			addr[ix] = Integer.parseInt(s1[ix]);
		}
		return addr;
	}

	public int compareTo(NetAddr another) {
		if (another == null) {
			return -1;
		}

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

	public boolean equals(NetAddr another) {
		if (another == null) {
			return false;
		}
		if (another instanceof NetAddr) {
			return compareTo((NetAddr)another) == 0;
		}
		return false;
	}

	public boolean within(NetAddr another) {
		int[] addr = another.getNetworkAddr();
		// 多分、0.0.0.0 => 非固定 のチェックは行わなくても大丈夫
		for (int ix = 0; ix < 4; ix++) {
			if (addr[ix] == network[ix]) {
				// 等しい場合は下位OCTの比較を行う
			}
			else if (addr[ix] < network[ix]) {
				// 小さい場合はIPアドレスのレンジ外
				return false;
			}
			else {
				// 大きい場合はBroadcastアドレスを比較する
				break;
			}
		}
		addr = another.getBroadcastAddr();
		for (int ix = 0; ix < 4; ix++) {
			if (addr[ix] == brdcast[ix]) {
				// 等しい場合は下位OCTの比較を行う
			}
			else if (addr[ix] > brdcast[ix]) {
				// 大きい場合はIPアドレスのレンジ外
				return false;
			}
			else {
				// 小さい場合はネットワークアドレスの範囲内
				return true;
			}
		}
		// 全てのOCTが等しい場合はネットワークアドレスの範囲内
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
	public int getNetmask() {
		return netmask;
	}
	// xx.xx.xx.xx (yy.yy.yy.yy-zz.zz.zz.zz)
	public String toStringRange() {
		StringBuilder sb = new StringBuilder(toString())
				.append(" (")
				.append(join(network, "."))
				.append("-")
				.append(join(brdcast, "."))
				.append(")");
		return sb.toString();
	}
	// xx.xx.xx.xx/yy
	public String toStringNetwork() {
		StringBuilder sb = new StringBuilder()
				.append(join(network, "."))
				.append("/")
				.append(netmask);
		return sb.toString();
	}
	@Override
	// xx.xx.xx.xx
	public String toString() {
		StringBuilder sb = new StringBuilder()
				.append(join(srcaddr, "."));
		return sb.toString();
	}

	private String join(int[] a, String d) {
		return new StringBuilder().append(a[0]).append(d).append(a[1]).append(d).append(a[2]).append(d).append(a[3]).toString();
	}
}
