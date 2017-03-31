package logcheck.known;

import logcheck.isp.IspList;

public class KnownListIsp extends IspList {

	public KnownListIsp(String name, String country) {
		super(name, country);
	}
	/*
	public String toString() {
		return String.format("%s (%s):%s", getName(), getCountry(), super.toString());
	}
	*/
}
