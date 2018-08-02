package logcheck.sdc;

import logcheck.isp.IspListImpl;
import logcheck.util.net.NetAddr;

public class SdcListIsp extends IspListImpl {

	private String netName = null;

	// network定義の場合はnameにはnetwork名
	// host定義の場合はnameには端末名が設定されている
	public SdcListIsp(String name, String type) {
		super(name, type);
	}

	@Override
	public String getName() {
		if (netName != null) {
			return netName;
		}
		return super.getName();
	}
	public void setName(String netName) {
		this.netName = netName;
	}

	@Override
	public String getHostname(NetAddr addr) {
		if (netName != null) {
			// host
			return super.getName();
		}
		// network
		return addr.toString();
	}

	@Override
	public String toString() {
		return String.format("name=%s, addr=%s", getName(), getAddress());
	}

	// equals()を実装するとhashCode()の実装も要求され、それはBugにランク付けられるのでequals()の実装をやめる
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

}
