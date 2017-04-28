package logcheck.fw;

import logcheck.util.ClientAddr;
import logcheck.util.NetAddr;

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
		//System.err.print(this);
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

	public int compareTo(FwLogBean bean) {
		// TODO Auto-generated method stub
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
		//return dstport - bean.getDstPort();
		return 0;
	}
	public String toString() {
		//return String.format("date=%s, srcip=%s, dstip=%s, dstport=%d", date, srcip, dstip, dstport);
		return String.format("srcip=%s, dstip=%s, dstport=%d", srcip, dstip, dstport);
	}
}
