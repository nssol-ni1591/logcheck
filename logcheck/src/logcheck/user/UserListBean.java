package logcheck.user;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import logcheck.user.sslindex.SSLIndexBean;
import logcheck.util.net.NetAddr;

public class UserListBean implements Comparable<UserListBean> {

	private final String userId;
	private final String userDelFlag;
	private String validFlag;
	private String expire;
	private String revoce;

	private final Set<UserListSite> sites;

	private static final DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyMMddHHmmss");
	private static final DateTimeFormatter format2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public UserListBean(SSLIndexBean b, String userDelFlag) {
		this.userId = b.getUserId();
		this.userDelFlag = userDelFlag;
		this.validFlag = b.getFlag();
		this.expire = b.getExpire();
		this.revoce = b.getRevoce();
		this.sites = new HashSet<>();
	}
	public UserListBean(String userId, String userDelFlag, String validFlag) {
		this.userId = userId;
		this.userDelFlag = userDelFlag;
		this.validFlag = validFlag;
		this.expire = "";
		this.revoce = "";
		this.sites = new HashSet<>();
	}

	public String getUserId() {
		return userId;
	}
	public String getUserDelFlag() {
		return userDelFlag;
	}
	public String getValidFlag() {
		return validFlag;
	}
	public String getExpire() {
		if ("".equals(expire)) {
			return "";
		}
		LocalDateTime d = LocalDateTime.parse(expire.substring(0, expire.length() - 1), format1);
		return d.format(format2);
	}
	public String getRevoce() {
		if ("".equals(revoce)) {
			return "";
		}
		LocalDateTime d = LocalDateTime.parse(revoce.substring(0, revoce.length() - 1), format1);
		return d.format(format2);
	}

	public Set<UserListSite> getSites() {
		return sites;
	}
	public void addSite(UserListSite site) {
		sites.add(site);
	}

	public UserListSite getSite(String siteId) {
		Optional<UserListSite> rc = sites.stream()
				.filter(site -> siteId.equals(site.getSiteId()))
				.findFirst();
		return rc.isPresent() ? rc.get() : null;
	}
	public UserListSite getSite(NetAddr addr) {
		Optional<UserListSite> rc = sites.stream()
				.filter(site -> site.within(addr))
				.findFirst();
		return rc.isPresent() ? rc.get() : null;
	}
	public UserListSite getSite(String projId, String siteName) {
		Optional<UserListSite> rc = sites.stream()
				.filter(site -> site.getProjId().equals(projId) && site.getSiteName().equals(siteName))
				.findFirst();
		return rc.isPresent() ? rc.get() : null;
	}
	public void update(SSLIndexBean b) {
		validFlag = b.getFlag();
		expire = b.getExpire();
		revoce = b.getRevoce();
	}

	public int getTotal() {
		return sites.stream().mapToInt(site -> site.getCount()).sum();
	}

	public String toString() {
		return String.format("userId=%s, del=%s, site=%s", userId, userDelFlag, sites);
	}

	@Override
	public int compareTo(UserListBean o) {
		return userId.compareTo(o.getUserId());
	}
}
