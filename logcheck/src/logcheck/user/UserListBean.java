package logcheck.user;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import logcheck.isp.IspList;
import logcheck.util.net.NetAddr;

public class UserListBean {

	private final String userId;
	private final String userDelFlag;
	private final String validFlag;

	private Set<UserListSummary> sites;

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

	public Set<UserListSummary> getSites() {
		return sites;
	}
	public void addSite(UserListSummary site) {
		sites.add(site);
	}
	public UserListSummary getSite(NetAddr addr) {
		Optional<UserListSummary> rc = sites.stream()
				.filter(site -> site.getAddress().stream().anyMatch(net -> net.within(addr)))
				.findFirst();
		return rc.isPresent() ? rc.get() : null;
	}
	public UserListSummary getSite(String projId, String name) {
		Optional<UserListSummary> rc = sites.stream()
				.filter(site -> site.getCountry().equals(projId) && site.getName().equals(name))
				.findFirst();
		return rc.isPresent() ? rc.get() : null;
	}

	public String toString() {
		return String.format("userId=%s, del=%s, site=%s", userId, userDelFlag, sites);
	}
}
