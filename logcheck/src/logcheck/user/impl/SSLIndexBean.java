package logcheck.user.impl;

public class SSLIndexBean {

	private final String flag;
	private final String expire;
	private final String revoce;
	private final String serial;
	private final String filename;
	private final String userId;

	public SSLIndexBean(String flag
			, String expire
			, String revoce
			, String serial
			, String filename
			, String userId
			) {
		this.flag = "V".equals(flag) ? "1" : "0";
		this.expire = expire;
		this.revoce = revoce;
		this.serial = serial;
		this.filename = filename;
		this.userId = userId;
	}

	public String getFlag() {
		return flag;
	}
	public String getExpire() {
		return expire;
	}
	public String getRevoce() {
		return revoce;
	}
	public String getSerial() {
		return serial;
	}
	public String getFilename() {
		return filename;
	}
	public String getUserId() {
		return userId;
	}

	@Override
	public String toString() {
		return "user=" + userId + ", flag=" + flag;
	}

}
