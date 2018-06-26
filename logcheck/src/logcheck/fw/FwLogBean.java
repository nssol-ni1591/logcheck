package logcheck.fw;

import logcheck.util.net.ClientAddr;
import logcheck.util.net.NetAddr;

public class FwLogBean implements Comparable<FwLogBean> {

	private final String date;
	// 2018/06/26 delete field: level;
	private final NetAddr srcip;
	// 2018/06/26 delete field: srcport;
	private final NetAddr dstip;
	private final int dstport;

	public FwLogBean(String date, String time, String level,
			String srcip, String srcport, String dstip, String dstport)
	{
		this.date = date + " " + time;
		this.srcip = new ClientAddr(srcip);
		this.dstip = new ClientAddr(dstip);
		this.dstport = dstport == null ? 0 : Integer.parseInt(dstport);
	}

	public String getDate() {
		return date;
	}
	public NetAddr getSrcIp() {
		return srcip;
	}
	public NetAddr getDstIp() {
		return dstip;
	}
	public int getDstPort() {
		return dstport;
	}

	@Override
	public int compareTo(FwLogBean bean) {
		int rc = 0;
		rc = bean.getDstPort() - dstport;
		if (rc != 0) {
			return rc;
		}
		rc = srcip.compareTo(bean.getSrcIp());
		if (rc != 0) {
			return rc;
		}
		rc = dstip.compareTo(bean.getDstIp());
		if (rc != 0) {
			return rc;
		}
		return 0;
	}
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		if (getClass() != o.getClass()) {
			return false;
		}
		if (o instanceof FwLogBean) {
			return compareTo((FwLogBean)o) == 0;
		}
		return false;
	}
	@Override
	public String toString() {
		return String.format("srcip=%s, dstip=%s, dstport=%d", srcip, dstip, dstport);
	}
}
