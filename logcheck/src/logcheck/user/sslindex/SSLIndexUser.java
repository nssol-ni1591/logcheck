package logcheck.user.sslindex;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Optional;

import logcheck.util.net.NetAddr;

public class SSLIndexUser extends LinkedHashSet<SSLIndexSite> {

	private static final long serialVersionUID = 1L;

	private final String userId;
	private String validFlag;
	private String revoce;

	private static final DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyMMddHHmmss");
	private static final DateTimeFormatter format2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public SSLIndexUser(String userId, String validFlag, String revoce) {
		this.userId = userId;
		this.validFlag = validFlag;
		this.revoce = revoce;
	}

	public String getUserId() {
		return userId;
	}
	public String getValidFlag() {
		return validFlag;
	}
	public String getRevoce() {
		if ("".equals(revoce)) {
			return "";
		}
		LocalDateTime d = LocalDateTime.parse(revoce.substring(0, revoce.length() - 1), format1);
		return d.format(format2);
	}

	public SSLIndexSite get(NetAddr addr) {
		Optional<SSLIndexSite> rc = this.stream().filter(site -> site.within(addr)).findFirst();
		return rc.isPresent() ? rc.get() : null;
	}

	public String toString() {
		return String.format("user=%s, flag=%s, site=%s", userId, validFlag, super.toString());
	}

}
