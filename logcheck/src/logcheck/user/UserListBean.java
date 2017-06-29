package logcheck.user;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import logcheck.user.sslindex.SSLIndexBean;
import logcheck.util.net.NetAddr;

public class UserListBean implements Comparable<UserListBean> {

	private final String userId;
	private final String userDelFlag;
	private String validFlag;

	private final Set<UserListSite> sites;

	public UserListBean(String userId, String userDelFlag, String validFlag) {
		this.userId = userId;
		this.userDelFlag = userDelFlag;
		this.validFlag = validFlag;
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
				.filter(site -> site.getAddress().stream().anyMatch(net -> net.within(addr)))
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
	}

	public String toString() {
		return String.format("userId=%s, del=%s, site=%s", userId, userDelFlag, sites);
	}

	@Override
	public int compareTo(UserListBean o) {
		return userId.compareTo(o.getUserId());
	}
}
