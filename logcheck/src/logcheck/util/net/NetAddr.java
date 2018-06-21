package logcheck.util.net;

import java.util.logging.Level;
import java.util.logging.Logger;

public class NetAddr implements Comparable<NetAddr> {

	private final Logger log = Logger.getLogger(NetAddr.class.getName());

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
			// "xx.xx.xx.xx - yy.yy.yy.yy"形式
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
			// "xx.xx.xx.xx/yy"形式
			this.network = new int[4];
			this.brdcast = new int[4];
			range(netaddr);
			if (addr == null) {
				// Deep copyの方がよいか？
				this.srcaddr = this.network;
			}
			else {
				this.srcaddr = address(addr);
			}
		}
	}
	public NetAddr(String netaddr) {
		this(null, netaddr);
	}

	/*
	 *  "xx.xx.xx.xx/yy形式からnetwork、broadcast、netmaskを求める
	 */
	private void range(String addr) {
		// "xx.xx.xx.xx(/yy)?"形式であることを確認
		String[] s0 = addr.split("/");
		if (s0.length != 1 && s0.length != 2) {
			throw new IllegalArgumentException("network error: " + addr);
		}

		int[] array = address(s0[0]);
		for (int ix = 0; ix < 4; ix++) {
			this.network[ix] = array[ix];
			this.brdcast[ix] = array[ix];
		}

		// netmaskの取得
		if (s0.length < 2) {
			// netmaskの指定がない場合=>32bit固定
			this.netmask = 32;
		}
		else {
			// netmaskの指定がある場合y=>netmaskの書式を確認
			String[] s2 = s0[1].split("\\.");
			if (s2.length == 1) {
				// xx.xx.xx.xx/yy 形式
				this.netmask = Byte.parseByte(s0[1]);
			}
			else if (s2.length == 4) {
				// xx.xx.xx.xx/yy.yy.yy.yy 形式
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
						throw new IllegalArgumentException("netmask value: addr=" + addr + ", s2=" + s2);
					}
				}
				this.netmask = tmp;
			}
			else {
				// 上記2形式のどれにも当てはまらない場合はエラー
				throw new IllegalArgumentException("netmask format: addr=" + addr + ", array.len=" + s2.length);
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
	private int netmask(int[] network, int[] brdcast) {
		int tmp = 0;
		for (int ix = 3; ix >= 0; --ix) {
			int d = brdcast[ix] - network[ix];
			if (d == 255) {
				tmp += 8;
			}
			else {
				switch (d) {
				case 0:		tmp += 0; break;
				case 1:		tmp += 1; break;
				case 3:		tmp += 2; break;
				case 7:		tmp += 3; break;
				case 15:	tmp += 4; break;
				case 31:	tmp += 5; break;
				case 63:	tmp += 6; break;
				case 127:	tmp += 7; break;
				default:
					// Whoisに登録されているinetnumが不正な値の場合がある
					log.log(Level.WARNING, "illegal inetnum: network={0}, brdcast={1}", 
							new Object[] { toIpaddr(network), toIpaddr(brdcast) });
					if (d > 1) {
						tmp++;
					}
					if (d > 3) {
						tmp++;
					}
					if (d > 7) {
						tmp++;
					}
					if (d > 15) {
						tmp++;
					}
					if (d > 31) {
						tmp++;
					}
					if (d > 63) {
						tmp++;
					}
					if (d > 127) {
						tmp++;
					}
				}
				break;
			}
		}
		return 32 - tmp;
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
	@Override
	public boolean equals(Object another) {
		if (another == null) {
			return false;
		}
		if (another == this) {
			return true;
		}
		if (getClass() != another.getClass()) {
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
			else {
				// 大きい場合はIPアドレスのレンジ外なのでfalse
				// 小さい場合はネットワークアドレスの範囲内なのでtrue
				return addr[ix] < brdcast[ix];
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
				.append(toIpaddr(network))
				.append("-")
				.append(toIpaddr(brdcast))
				.append(")");
		return sb.toString();
	}
	// xx.xx.xx.xx/yy
	public String toStringNetwork() {
		StringBuilder sb = new StringBuilder()
				.append(toIpaddr(network))
				.append("/")
				.append(netmask);
		return sb.toString();
	}
	@Override
	// xx.xx.xx.xx
	public String toString() {
		StringBuilder sb = new StringBuilder()
				.append(toIpaddr(srcaddr));
		return sb.toString();
	}

	private String toIpaddr(int[] a) {
		String d = ".";
		return new StringBuilder().append(a[0]).append(d).append(a[1]).append(d).append(a[2]).append(d).append(a[3]).toString();
	}

}
