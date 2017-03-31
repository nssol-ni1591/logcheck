package logcheck.log;

import logcheck.isp.IspMap;
import logcheck.util.NetAddr;

public class AccessLogSummary extends IspMap<Integer> {

	public AccessLogSummary(String name) {
		super(name);
	}
	public AccessLogSummary(String name, String country) {
		super(name, country);
	}

	public int sum() {
		return getRef().values().stream().mapToInt(Integer::intValue).sum();
	}
	public void addAddress(NetAddr addr) {
		Integer count = get(addr);
		if (count == null) {
			count = new Integer(0);
		}
		count += 1;
		put(addr, count);
	}

	public String toString() {
		return "name=" + getName()+ ", sum=" + sum();
	}
}
