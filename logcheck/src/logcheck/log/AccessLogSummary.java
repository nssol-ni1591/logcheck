package logcheck.log;

import java.util.Objects;

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
	public synchronized void update(AccessLogSummary sum) {
		super.update(sum.getFirstDate());
		if (sum.getRoles() != null) {
			roles = sum.getRoles();
		}
	}
	@Override
	public int compareTo(AccessLogSummary sum) {
		Objects.requireNonNull(sum);

		int rc = Objects.compare(usrId, sum.getId(), (o1, o2) -> o1.compareTo(o2));
		if (rc != 0) {
			return rc;
		}
		rc = Objects.compare(addr, sum.getAddr(), (o1, o2) -> o1.compareTo(o2));
		if (rc != 0) {
			return rc;
		}
		//ispにはaddrに一致するISP情報が設定されているはずなのでispの比較は不要
		return 0;
		/*
		if- (isp == null) {	-
			return sum.getIsp() == null ? 0 : 1;	-
		}	-
		return isp.compareTo(sum.getIsp());	-
		*/
	}
	// equals()を実装するとhashCode()の実装も要求され、それはBugにランク付けられるのでequals()の実装をやめたいのだが
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		Objects.requireNonNull(o);

		if (o instanceof AccessLogSummary) {
			AccessLogSummary sum = (AccessLogSummary)o;
			if (usrId.equals(sum.getId())
					&& addr.equals(sum.getAddr())) {
				if (isp == null) {
					return sum.getIsp() == null;
				}
				return isp.equals(sum.getIsp());
			}
		}
		return false;
	}

}
