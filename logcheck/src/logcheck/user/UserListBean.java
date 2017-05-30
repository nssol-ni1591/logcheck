package logcheck.user;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import logcheck.isp.IspList;
import logcheck.log.AccessLogBean;
import logcheck.mag.MagListIsp;
import logcheck.util.net.NetAddr;

public class UserListBean {

	private final String userId;
	private final String userDelFlag;
	private final String validFlag;

	private Set<UserListSummary> list;

	public UserListBean(String userId, String userDelFlag, String validFlag) {
		this.userId = userId;
		this.userDelFlag = userDelFlag;
		this.validFlag = validFlag;
		this.list = new HashSet<>();
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
		return list;
	}
	public void addSite(UserListSummary site) {
		list.add(site);
	}
	public UserListSummary getSite(NetAddr addr) {
		Optional<UserListSummary> rc = list.stream().filter(site -> {
			return site.getAddress().stream().filter(net -> net.within(addr)).findFirst().isPresent();
		}).findFirst();
		return rc.isPresent() ? rc.get() : null;
	}
	public UserListSummary getSite(String projId, String name) {
		Optional<UserListSummary> rc = list.stream().filter(site -> site.getProjId().equals(projId) && site.getName().equals(name)).findFirst();
		return rc.isPresent() ? rc.get() : null;
	}

	public boolean isDelFlag() {
		for (UserListSummary site : list) {
			if (!site.isDelFlag()) {
				return false;
			}
		}
		return true;
	}

	public void update(AccessLogBean b, UserListSummary site) {
		site.update(b.getDate());
	}
	public void update(AccessLogBean b, IspList isp) {
		UserListSummary site = new UserListSummary(isp);
		addSite(site);
		site.update(b.getDate());
	}
	public void update(AccessLogBean b, MagListIsp isp) {
		UserListSummary site = new UserListSummary(isp);
		addSite(site);
		site.update(b.getDate());
	}
	
	public int sumCount() {
		return list.stream().mapToInt(site -> site.getCount()).sum();
	}

	public String toString() {
		return String.format("userId=%s, del=%s, site=%s", userId, userDelFlag, list);
	}
}
