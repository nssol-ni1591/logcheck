package logcheck.fw;

import java.util.Objects;

import logcheck.util.ClientAddr;
import logcheck.util.NetAddr;

public class FwLogBean implements Comparable<FwLogBean> {

	private final String date;
	private final String level;
	private final NetAddr srcip;
	private final int srcport;
	private final NetAddr dstip;
	private final int dstport;

	public FwLogBean(String date, String time, String level,
			String srcip, String srcport, String dstip, String dstport)
	{
		this.date = date + " " + time;
		this.level = level;
		this.srcip = new ClientAddr(srcip);
		this.dstip = new ClientAddr(dstip);
		this.srcport = srcport == null ? 0 : Integer.parseInt(srcport);
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
		Objects.requireNonNull(bean);

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
		Objects.requireNonNull(o);

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
