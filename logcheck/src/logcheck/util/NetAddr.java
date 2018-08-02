package logcheck.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class NetAddr implements Comparable<NetAddr> {

	private Logger log = Logger.getLogger(NetAddr.class.getName());

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
		Objects.requireNonNull(netaddr);

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

		// netmask の取得
		this.netmask = netmask(s0, addr);

		// networkアドレスと broadcastアドレスの設定
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

	// netmaskの取得
	private int netmask(String[] s0, String addr) {
		int mask = 0;
		if (s0.length < 2) {
			// netmaskの指定がない場合=>32bit固定
			mask = 32;
		}
		else {
			// netmaskの指定がある場合y=>netmaskの書式を確認
			String[] s2 = s0[1].split("\\.");
			if (s2.length == 1) {
				// xx.xx.xx.xx/yy 形式
				mask = Byte.parseByte(s0[1]);
			}
			else if (s2.length == 4) {
				// xx.xx.xx.xx/yy.yy.yy.yy 形式
				for (int ix = 0; ix < 4; ix++) {
					int m = Integer.parseInt(s2[ix]);

					switch (m) {
					case 0:		mask += 0 ; break;
//					case 127:	mask += 1 ; break;	// 記入ミス対応
					case 128:	mask += 1 ; break;
					case 192:	mask += 2 ; break;
					case 224:	mask += 3 ; break;
					case 240:	mask += 4 ; break;
					case 248:	mask += 5 ; break;
					case 252:	mask += 6 ; break;
					case 254:	mask += 7 ; break;
					case 255:	mask += 8 ; break;
					default:
						throw new IllegalArgumentException("netmask value: addr=" + addr + ", s2=" + s2);
					}
				}
			}
			else {
				// 上記2形式のどれにも当てはまらない場合はエラー
				throw new IllegalArgumentException("netmask format: addr=" + addr + ", array.len=" + s2.length);
			}
		}
		return mask;
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
					// 本来ならばエラーだが、このNetAddrの実装でNetmaskの必要性は低いので出力レベルを落とす
					log.log(Level.INFO, "illegal inetnum: network={0}, brdcast={1}", 
							new Object[] { toIpaddr(network), toIpaddr(brdcast) });

					// Whoisに登録されているinetnumが不正な値の場合があるので、適当に補正する
					tmp += errorNetmask(d);
				}
				break;
			}
		}
		return 32 - tmp;
	}
	private int errorNetmask(int d) {
		int tmp = 0;
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
		return tmp;
	}

	// IPアドレスの型変換
	private int[] address(String s) {
		Objects.requireNonNull(s);

		String[] s1 = s.split("\\.");
		if (s1.length > 4) {
			throw new IllegalArgumentException("format error: addr=" + s + ", len=" + s1.length);
		}
		int[] addr = new int[4];
		for (int ix = 0; ix < s1.length; ix++) {
			addr[ix] = Integer.parseInt(s1[ix]);
		}
		return addr;
	}


	public int compareTo(NetAddr another) {
		Objects.requireNonNull(another);

		int[] addr = another.getNetworkAddr();
		for (int ix = 0; ix < 4; ix++) {
			if (network[ix] - addr[ix] != 0) {
				return addr[ix] - network[ix];
			}
		}
		addr = another.getBroadcastAddr();
		for (int ix = 0; ix < 4; ix++) {
			if (brdcast[ix] - addr[ix] != 0) {
				return addr[ix] - brdcast[ix];
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
		Objects.requireNonNull(another);

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
		Objects.requireNonNull(another);

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

	protected String toIpaddr(int[] a) {
		//この実装が一番理解しやすい
		return Arrays.stream(a).mapToObj(String::valueOf).collect(Collectors.joining("."));
		/* 次席
		return Arrays.stream(a).collect(
				StringBuilder::new
				, (t, i) -> t.append(".").append(i)
				, (t, u) -> t.append(u)
				)
				.substring(1);	// 1文字目の"."を削除
		 */
		/*
		return Arrays.stream(a).collect(
				StringBuilder::new
				, (t, i) -> {	-
					//System.out.println("t=" + t + ", i=" + i);	-
					-f (t.length() == 0) {	-
						t.append(i);	-
					}	-
					-lse { -
						t.append(".").append(i);	-
					}	-
				}	-
				, (t, u) -> {	-
					// この場合、この実装が呼ばれない ... なぜ?
					// -> 2つのstreamを1つにまとめる。つまり、parallel()でないと使用されない
					System.out.println("t=" + t + ", u=" + u);	-
					t.append(u);	-
				})	-
				.toString();	-
		 */
		/* Stringは<R>に使用できないらしい
		return Arrays.stream(a).collect(
				String::new
				, (t, i) -> t + "." + i
				, (t, u) -> t + u
				)
		 */
	}

}
