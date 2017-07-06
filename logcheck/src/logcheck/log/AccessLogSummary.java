package logcheck.log;

import logcheck.isp.Isp;
import logcheck.util.Summary;
import logcheck.util.net.NetAddr;

public class AccessLogSummary extends Summary<String> implements Comparable<AccessLogSummary> {

	private final NetAddr addr;
	private final String usrId;
	private final Isp isp;

	private String roles;
	
	private String afterUsrId = "";		// 直後の同じIPアドレスからの認証正常ログのユーザID
	private String reason = "";
	private String detail = "";

	public AccessLogSummary(AccessLogBean log, String pattern) {
		super(pattern, log.getDate());
		this.addr = log.getAddr();
		this.usrId = log.getId();
		this.isp = null;
		this.roles = log.getRoles();
	}
	public AccessLogSummary(AccessLogBean log, String pattern, Isp isp) {
		super(pattern, log.getDate());
		this.addr = log.getAddr();
		this.usrId = log.getId();
		this.isp = isp;
		this.roles = log.getRoles();
	}

	public NetAddr getAddr() {
		return addr;
	}
	public String getId() {
		return usrId;
	}
	public String getPattern() {
		return getRef();
	}
	public Isp getIsp() {
		return isp;
	}
	public String getRoles() {
		return roles;
	}

	public String getAfterUsrId() {
		return afterUsrId;
	}
	public void setAfterUsrId(String afterUsrId) {
		this.afterUsrId = afterUsrId;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}

	public synchronized void update(AccessLogBean b) {
		super.update(b.getDate());
		if ("".equals(roles)) {
			roles = b.getRoles();
		}
	}

	@Override
	public int compareTo(AccessLogSummary o) {
		// TODO Auto-generated method stub
		return getRef().compareTo(o.getRef());
	}
	
	public String toString() {
		return String.format("[first=%s, last=%s, addr=%s, id=%s, count=%d]",
				getFirstDate(), getLastDate(), addr.toString(), usrId, getCount());
	}

}
