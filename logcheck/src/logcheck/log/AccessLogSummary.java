package logcheck.log;

import logcheck.isp.Isp;
import logcheck.util.NetAddr;

public class AccessLogSummary implements Comparable<AccessLogSummary> {

	private final NetAddr addr;
	private final String usrId;
	private final String pattern;
	private final Isp isp;

	private String firstDate;
	private String lastDate;
	private String roles;
	private int count;
	
	private String afterUsrId = "";		// 直後の同じIPアドレスからの認証正常ログのユーザID
	private String reason = "";
	private String detail = "";

	public AccessLogSummary(AccessLogBean log, String pattern) {
		this.addr = log.getAddr();
		this.usrId = log.getId();
		this.pattern = pattern;
		this.isp = null;

		this.firstDate = log.getDate();
		this.lastDate = firstDate;
		this.roles = log.getRoles();
		this.count = 1;
	}
	public AccessLogSummary(AccessLogBean log, String pattern, Isp isp) {
//		this(log, pattern);
		this.addr = log.getAddr();
		this.usrId = log.getId();
		this.pattern = pattern;
		this.isp = isp;

		this.firstDate = log.getDate();
		this.lastDate = firstDate;
		this.roles = log.getRoles();
		this.count = 1;
	}

	public String getFirstDate() {
		return firstDate;
	}
	public String getLastDate() {
		return lastDate;
	}
	public NetAddr getAddr() {
		return addr;
	}
	public String getId() {
		return usrId;
	}
	public String getPattern() {
		return pattern;
	}
	public Isp getIsp() {
		return isp;
	}
	public String getRoles() {
		return roles;
	}
	public int getCount() {
		return count;
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
		String date = b.getDate();
		if (firstDate.compareTo(date) > 0) {
			this.firstDate = date;
		}
		if (lastDate.compareTo(date) < 0) {
			this.lastDate = date;
		}

		if ("".equals(roles)) {
			roles = b.getRoles();
		}
		this.count += 1;
	}
	public void addCount() {
		this.count += 1;
	}

	@Override
	public int compareTo(AccessLogSummary o) {
		// TODO Auto-generated method stub
		return pattern.compareTo(o.getPattern());
	}
	
	public String toString() {
		return String.format("[first=%s, last=%s, addr=%s, id=%s, count=%d]", firstDate, lastDate, addr.toString(), usrId, count);
	}
}
