package logcheck.user;

import java.util.HashSet;
import java.util.Set;

public class UserListBean {

	private final String userId;
	private Set<UserListSite> list;

	public UserListBean(String userId) {
		this.userId = userId;
		this.list = new HashSet<>();
	}

	public String getUserId() {
		return userId;
	}
	public Set<UserListSite> getPrjs() {
		return list;
	}
	public void addPrjs(UserListSite prj) {
		list.add(prj);
	}

	public String toString() {
		return String.format("userId=%s prjs=%s", userId, list);
	}
}
