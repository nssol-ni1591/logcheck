package logcheck.user.sslindex;

import java.util.LinkedHashSet;
import java.util.Optional;

import logcheck.util.net.NetAddr;

public class SSLIndexUser extends LinkedHashSet<SSLIndexSite> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String userId;
	private final String flag;

	public SSLIndexUser(String userId, String flag) {
		this.userId = userId;
		this.flag = flag;
	}

	public String getUserId() {
		return userId;
	}
	public String getFlag() {
		return flag;
	}

	public SSLIndexSite get(NetAddr addr) {
		Optional<SSLIndexSite> rc = this.stream().filter(site -> site.within(addr)).findFirst();
		return rc.isPresent() ? rc.get() : null;
	}

	public String toString() {
		return String.format("user=%s, flag=%s, site=%s", userId, flag, super.toString());
	}

}
