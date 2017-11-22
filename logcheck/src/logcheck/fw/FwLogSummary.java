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

	public FwLogSummary(FwLogBean bean) {
		super(null, bean.getDate());
		this.dstPort = bean.getDstPort();
		this.srcAddr = bean.getSrcIp();
		this.dstAddr = bean.getDstIp();
		this.srcIsp = null;
		this.dstIsp = null;
		// callされた時点で1回目のログがあるため初期値を1にする
		super.addCount();
	}
	public FwLogSummary(FwLogBean bean, Isp srcIsp, Isp dstIsp) {
		super(null, bean.getDate());
		this.srcAddr = bean.getSrcIp();
		this.dstAddr = bean.getDstIp();
		this.dstPort = bean.getDstPort();
		this.srcIsp = srcIsp;
		this.dstIsp = dstIsp;
// callされた時点で1回目のログがあるため初期値を1にする
		super.addCount();
	}

	@Override
	public String getFirstDate() {
		return super.getLastDate();
	}
	@Override
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
