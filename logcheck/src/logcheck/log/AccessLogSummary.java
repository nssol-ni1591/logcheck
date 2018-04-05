package logcheck.log;

import logcheck.isp.Isp;
import logcheck.util.Summary;
import logcheck.util.net.NetAddr;

public class AccessLogSummary extends Summary<String> implements Comparable<AccessLogSummary> {

	private final NetAddr addr;
	private final String usrId;
	private final Isp isp;

	private String[] roles;
	
	private String afterUsrId = "";		// 直後の同じIPアドレスからの認証正常ログのユーザID
	private String reason = "";
	private String detail = "";

	public AccessLogSummary(AccessLogBean b, String pattern) {
		super(pattern, b.getDate());
		this.addr = b.getAddr();
		this.usrId = b.getId();
		this.isp = null;
		this.roles = b.getRoles();
		// クラス生成時に1回呼び出されているので
		super.addCount();
	}
	public AccessLogSummary(AccessLogBean b, String pattern, Isp isp) {
		super(pattern, b.getDate());
		this.addr = b.getAddr();
		this.usrId = b.getId();
		this.isp = isp;
		this.roles = b.getRoles();
		// クラス生成時に1回呼び出されているので
		super.addCount();
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
	public String[] getRoles() {
		if (roles == null) {
			return new String[] { "" };
		}
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
		if (b.getRoles() != null) {
			roles = b.getRoles();
		}
	}

	@Override
	public int compareTo(AccessLogSummary o) {
		return getRef().compareTo(o.getRef());
	}
	// equals()を実装するとhashCode()の実装も要求され、それはBugにランク付けられるのでequals()の実装をやめたいのだが
	/*
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof AccessLogSummary) {
			AccessLogSummary bean = (AccessLogSummary)o;
			return compareTo(bean) == 0;
		}
		return false;
	}
	*/
	@Override
	public String toString() {
		return String.format("[first=%s, last=%s, addr=%s, id=%s, count=%d]",
				getFirstDate(), getLastDate(), addr.toString(), usrId, getCount());
	}

}
