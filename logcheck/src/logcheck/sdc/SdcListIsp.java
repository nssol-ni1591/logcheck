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

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}
	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
