package logcheck.msg;

import java.util.Set;

import logcheck.isp.IspList;
import logcheck.isp.IspMap;
import logcheck.util.NetAddr;

public class MsgMapIsp extends IspMap<Integer> {

	private IspList isp;

	public MsgMapIsp(String name, IspList isp) {
		super(name, null);
		this.isp = isp;
	}

	public IspList getIsp() {
		return isp;
	}

	public Set<NetAddr> getAddress() {
		return getRef().keySet();
	}
	public void addAddress(NetAddr addr) {
		Integer count = get(addr);
		if (count == null) {
			count = new Integer(0);
		}
		count += 1;
		put(addr, count);
	}
	public int getCount(NetAddr addr) {
		return get(addr);
	}
	public int getSum() {
		return getAddress().stream().mapToInt(addr -> getCount(addr)).sum();
	}
}
