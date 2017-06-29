package logcheck.fw;

import logcheck.isp.Isp;
import logcheck.util.Summary;
import logcheck.util.net.NetAddr;

public class FwLogSummary extends Summary<FwLogBean> implements Comparable<FwLogSummary> {

	private final Isp srcIsp;
	private final NetAddr srcAddr;
	private final Isp dstIsp;
	private final NetAddr dstAddr;
	private final int dstPort;

//	private String firstDate;
//	private final String lastDate;
//	private int count;

	public FwLogSummary(FwLogBean bean) {
		super(null, bean.getDate());
//		this.firstDate = bean.getDate();
//		this.lastDate = bean.getDate();
		this.dstPort = bean.getDstPort();
		this.srcAddr = bean.getSrcIp();
		this.dstAddr = bean.getDstIp();
		this.srcIsp = null;
		this.dstIsp = null;
// callされた時点で1回目のログがあるため
//		this.count = 1;
		super.addCount();
	}
	public FwLogSummary(FwLogBean bean, Isp srcIsp, Isp dstIsp) {
		super(null, bean.getDate());
//		this.firstDate = bean.getDate();
//		this.lastDate = bean.getDate();
		this.srcAddr = bean.getSrcIp();
		this.dstAddr = bean.getDstIp();
		this.dstPort = bean.getDstPort();
		this.srcIsp = srcIsp;
		this.dstIsp = dstIsp;
// callされた時点で1回目のログがあるため
//		this.count = 1;
		super.addCount();
	}

//	public FwLogBean getBean() {
//		return bean;
//	}
	// fwログは順番が逆のため
	public String getFirstDate() {
		return super.getLastDate();
	}
	public String getLastDate() {
		return super.getFirstDate();
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
//	@Override
//	public int getCount() {
//		return count;
//	}

//	public synchronized void update(FwLogBean bean) {
//		firstDate = bean.getDate();
//		count += 1;
//	}

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
		return "[" + getFirstDate() + "/" + getLastDate() + "]"
				+ ", src=" + srcAddr + ", dst=" + dstAddr + ", count=" + getCount();
	}

}
