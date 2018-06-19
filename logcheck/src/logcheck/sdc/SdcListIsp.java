package logcheck.sdc;

import logcheck.isp.IspListImpl;

public class SdcListIsp extends IspListImpl {

	public SdcListIsp(String name, String type) {
		super(name, type);
	}

	@Override
	public String toString() {
		return String.format("name=%s, addr=%s", getName(), getAddress());
	}
	// equals()を実装するとhashCode()の実装も要求され、それはBugにランク付けられるのでequals()の実装をやめる
}
