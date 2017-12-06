package logcheck.fw;

import logcheck.util.net.ClientAddr;
import logcheck.util.net.NetAddr;

public class FwLogBean implements Comparable<FwLogBean> {

	private final String date;
	private final String level;
	private final NetAddr srcip;
	private final int srcport;
	private final NetAddr dstip;
	private final int dstport;

	public FwLogBean(String date, String time, String level, String srcip, String srcport, String dstip, String dstport) {
		this.date = date + " " + time;
		this.level = level;
		this.srcip = new ClientAddr(srcip);
		this.srcport = srcport == null ? 0 : Integer.parseInt(srcport);
		this.dstip = new ClientAddr(dstip);
		this.dstport = dstport == null ? 0 : Integer.parseInt(dstport);
	}

	public String getDate() {
		return date;
	}
	public String getLevel() {
		return level;
	}
	public NetAddr getSrcIp() {
		return srcip;
	}
	public int getSrcPort() {
		return srcport;
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
		rc = dstport - bean.getDstPort();
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
	/*
	equals()を実装するとhashCode()の実装も要求され、それはBugにランク付けられるのでequals()の実装をやめる
	*/
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof FwLogBean) {
			FwLogBean bean = (FwLogBean)o;
			return compareTo(bean) == 0;
		}
		return false;
	}
	@Override
	public String toString() {
		return String.format("srcip=%s, dstip=%s, dstport=%d", srcip, dstip, dstport);
	}
}
