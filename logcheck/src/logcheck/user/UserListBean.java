package logcheck.user;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import logcheck.isp.IspList;
import logcheck.util.net.NetAddr;

public class UserListBean<E extends IspList> {

	private final String userId;
	private final String userDelFlag;
	private final String validFlag;

	private Set<E> list;

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

	public Set<E> getSites() {
		return list;
	}
	public void addSite(E site) {
		list.add(site);
	}
	public E getSite(NetAddr addr) {
		Optional<E> rc = list.stream()
/*
				.filter(site -> {
					return site.getAddress().stream().filter(net -> net.within(addr)).findFirst().isPresent();
				})
*/
				.filter(site -> site.getAddress().stream().anyMatch(net -> net.within(addr)))
				.findFirst();
		return rc.isPresent() ? rc.get() : null;
	}
	public E getSite(String projId, String name) {
		Optional<E> rc = list.stream()
				.filter(site -> site.getCountry().equals(projId) && site.getName().equals(name))
				.findFirst();
		return rc.isPresent() ? rc.get() : null;
	}
/*
	public boolean isDelFlag() {
		for (E site : list) {
			if (!site.isDelFlag()) {
				return false;
			}
		}
		return true;
	}
*/
/*
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
*/
/*
	public int sumCount() {
		return list.stream().mapToInt(site -> site.getCount()).sum();
	}
*/
	public String toString() {
		return String.format("userId=%s, del=%s, site=%s", userId, userDelFlag, list);
	}
}
