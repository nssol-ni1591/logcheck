package logcheck.user.sslindex;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class SSLIndexBean {

	private final String flag;
	private final String expire;
	private final String revoce;
	private final String serial;
	private final String filename;
	private final String userId;

	@Inject private Logger log;

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

	@PostConstruct
	public void init() {
		log.log(Level.FINE, "SSLIndex={0}", toString());
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
