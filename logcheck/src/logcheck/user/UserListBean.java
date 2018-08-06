package logcheck.user;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import logcheck.user.impl.SSLIndexBean;
import logcheck.util.NetAddr;

public class UserListBean implements Comparable<UserListBean> {

	private final String userId;
	private String validFlag;
	private String expire;
	private String revoce;

	private final Set<UserListSite> sites;

	private static final DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyMMddHHmmss");
	private static final DateTimeFormatter format2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public UserListBean(SSLIndexBean b) {
		this.userId = b.getUserId();
		this.validFlag = b.getFlag();
		this.expire = b.getExpire();
		this.revoce = b.getRevoce();
		this.sites = new LinkedHashSet<>();
	}
	public UserListBean(String userId) {
		this.userId = userId;
		this.sites = new LinkedHashSet<>();
	}
	public UserListBean(String userId, String validFlag) {
		this.userId = userId;
		this.validFlag = validFlag;
		this.expire = "";
		this.revoce = "";
		this.sites = new LinkedHashSet<>();
	}

	public String getUserId() {
		return userId;
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
	public synchronized void update(SSLIndexBean b) {
		validFlag = b.getFlag();
		expire = b.getExpire();
		revoce = b.getRevoce();
	}

	public int getTotal() {
		return sites.stream().mapToInt(UserListSite::getCount).sum();
	}

	public String getProjDelFlag() {
		if (sites.stream().allMatch(site -> "-1".equals(site.getProjDelFlag()))) {
			return "-1";
		}
		return sites.stream().anyMatch(site -> "0".equals(site.getProjDelFlag())) ? "0" : "1";
	}
	public String getSiteDelFlag() {
		if (sites.stream().allMatch(site -> "-1".equals(site.getSiteDelFlag()))) {
			return "-1";
		}
		return sites.stream().anyMatch(site -> "0".equals(site.getSiteDelFlag())) ? "0" : "1";
	}
	public String getUserDelFlag() {
		if (sites.stream().allMatch(site -> "-1".equals(site.getUserDelFlag()))) {
			return "-1";
		}
		return sites.stream().anyMatch(site -> "0".equals(site.getUserDelFlag())) ? "0" : "1";
	}
	public String getFirstDate() {
		String date = "9999";
		for (UserListSite site : sites) {
			if (site.getFirstDate().compareTo(date) < 0) {
				date = site.getFirstDate();
			}
		}
		return date;
	}
	public String getLastDate() {
		String date = "";
		for (UserListSite site : sites) {
			if (site.getLastDate().compareTo(date) > 0) {
				date = site.getLastDate();
			}
		}
		return date;
	}
	@Override
	public String toString() {
		return String.format("userId=%s, valid=%s, site=%s", userId, validFlag, sites);
	}

	@Override
	public int compareTo(UserListBean o) {
		return userId.compareTo(o.getUserId());
	}
	// equals()を実装するとhashCode()の実装も要求され、それはBugにランク付けられるのでequals()の実装をやめる
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o instanceof UserListBean) {
			return this.compareTo((UserListBean) o) == 0;
		}
		return false;
	}

}
