package logcheck.user.sslindex;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class SSLIndexBean {

	private final String flag;
	private final LocalDateTime expire;
	private final String revoce;
	private final String serial;
	private final String filename;
	private final String userId;

	@Inject private Logger log;

	private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyMMddHHmmss");

	public SSLIndexBean(String flag
			, String expire
			, String revoce
			, String serial
			, String filename
			, String userId
			) {
		this.flag = "V".equals(flag) ? "1" : "0";
		this.expire = LocalDateTime.parse(expire.substring(0, expire.length() - 1), format);
		this.revoce = revoce; //LocalDateTime.parse("".equals(revoce) ? "" : revoce.substring(0, revoce.length() - 1), format);
		this.serial = serial;
		this.filename = filename;
		this.userId = userId;
	}

	@PostConstruct
	public void init() {
		log.fine(this.toString());
	}
	public String getFlag() {
		return flag;
	}
	public LocalDateTime getExpire() {
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
		return "user=" + userId + ", flag=" + flag + ", expire=" + expire;
	}
}
