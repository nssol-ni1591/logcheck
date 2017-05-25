package logcheck.user;

import java.util.HashSet;
import java.util.Set;

public class UserListBean {

	private final String userId;
	private final String userDelFlag;

	private Set<UserListSite> list;

	public UserListBean(String userId, String userDelFlag) {
		this.userId = userId;
		this.userDelFlag = userDelFlag;
		this.list = new HashSet<>();
	}

	public String getUserId() {
		return userId;
	}
	public String getUserDelFlag() {
		return userDelFlag;
	}
	public Set<UserListSite> getPrjs() {
		return list;
	}
	public void addPrjs(UserListSite prj) {
		list.add(prj);
	}

	public boolean isDelFlag() {
		for (UserListSite site : list) {
			if (!site.isDelFlag()) {
				return false;
			}
		}
		return true;
	}

	public String toString() {
		return String.format("userId=%s del=%s, site=%s", userId, userDelFlag, list);
	}
}
