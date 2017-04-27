package logcheck.sdc;

import logcheck.isp.IspList;

public class SdcListIsp extends IspList {

	public SdcListIsp(String name, String type) {
		super(name, type);
		//System.out.println(this);
	}
	/*
	public SdcListIsp(String name, String addr) {
		super(name, "SDC");
		addAddress(new NetAddr(addr));
		//System.out.println(this);
	}
	public SdcListIsp(String name, String addr, String type) {
		super(name, type);
		addAddress(new NetAddr(addr));
		//System.out.println(this);
	}
	*/
	/*
	public SdcListIsp(String name) {
		super(name, "SDC");
		//System.out.println(this);
	}
	*/
	/*
	public String toString() {
		return String.format("name=%s, addr=%s", getName(), getAddress());
	}
	*/
}
