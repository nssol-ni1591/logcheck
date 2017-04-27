package logcheck.fw;

import logcheck.isp.Isp;
import logcheck.util.NetAddr;

public class FwLogSummary implements Comparable<FwLogSummary> {

	private String firstDate;
	private final String lastDate;
	private final Isp srcIsp;
	private final NetAddr srcAddr;
	private final Isp dstIsp;
	private final NetAddr dstAddr;
	private final int dstPort;
	private int count;

	public FwLogSummary(FwLogBean bean) {
		this.firstDate = bean.getDate();
		this.lastDate = bean.getDate();
		this.dstPort = bean.getDstPort();
		this.srcAddr = bean.getSrcIp();
		this.dstAddr = bean.getDstIp();
		this.srcIsp = null;
		this.dstIsp = null;
		this.count = 1;
	}
	public FwLogSummary(FwLogBean bean, Isp srcIsp, Isp dstIsp) {
		this.firstDate = bean.getDate();
		this.lastDate = bean.getDate();
		this.srcAddr = bean.getSrcIp();
		this.dstAddr = bean.getDstIp();
		this.dstPort = bean.getDstPort();
		this.srcIsp = srcIsp;
		this.dstIsp = dstIsp;
		this.count = 1;
	}

//	public FwLogBean getBean() {
//		return bean;
//	}
	public String getFirstDate() {
		return firstDate;
	}
	public String getLastDate() {
		return lastDate;
	}
	public NetAddr getSrcAddr() {
		return srcAddr;
	}
	public NetAddr getDstAddr() {
		return dstAddr;
	}
	public Isp getSrcIsp() {
		return srcIsp;
	}
	public Isp getDstIsp() {
		return dstIsp;
	}
	public int getDstPort() {
		return dstPort;
	}
	public int getCount() {
		return count;
	}

	public void update(FwLogBean bean) {
		firstDate = bean.getDate();
		count += 1;
	}

	public int compareTo(FwLogSummary summary) {
		// TODO Auto-generated method stub
		int rc;
		rc = dstPort - summary.getDstPort();
		if (rc != 0) {
			return rc;
		}
		rc = srcAddr.compareTo(summary.getSrcAddr());
		if (rc != 0) {
			return rc;
		}
		rc = dstAddr.compareTo(summary.getDstAddr());
		if (rc != 0) {
			return rc;
		}
		return 0;

	}
	public String toString() {
		return "[" + firstDate + "/" + lastDate + "], src=" + srcAddr + ", dst=" + dstAddr + ", count=" + count;
	}

}
