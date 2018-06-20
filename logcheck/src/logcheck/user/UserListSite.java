package logcheck.user;

import java.util.Set;

import logcheck.known.KnownListIsp;
import logcheck.site.SiteListIsp;
import logcheck.site.SiteListKnownIsp;
import logcheck.util.Summary;
import logcheck.util.net.NetAddr;

public class UserListSite extends Summary<SiteListIsp> {

	/*
	 * このシステムにおけるDBユーザ情報のプライマリキーは、拠点+ユーザIDなので、拠点側のクラスにDBユーザ情報の属性を保持する
	 */
	private final String userDelFlag;
	private final String endDate;

	public UserListSite(SiteListIsp site, String userDelFlag, String endDate) {
		super(site);
		this.userDelFlag = userDelFlag;
		this.endDate = endDate;
	}
	/*
	 * ユーザ管理をExcelで行っていた時の遺産
	 */
	public UserListSite(KnownListIsp isp) {
		super(new SiteListKnownIsp(isp));
		this.userDelFlag = "-1";
		this.endDate = "";
	}

	public String getUserDelFlag() {
		return userDelFlag;
	}
	public String getEndDate() {
		return endDate;
	}

	public String getCountry() {
		return getRef().getCountry();
	}
	public String getProjId() {
		return getRef().getProjId();
	}
	public String getSiteId() {
		return getRef().getSiteId();
	}
	public String getSiteName() {
		return getRef().getSiteName();
	}
	public String getProjDelFlag() {
		return getRef().getProjDelFlag();
	}
	public String getSiteDelFlag() {
		return getRef().getSiteDelFlag();
	}

	public Set<NetAddr> getAddress() {
		return getRef().getAddress();
	}
	public void addAddress(NetAddr addr) {
		getRef().addAddress(addr);
	}
	public boolean within(NetAddr addr) {
		return getRef().within(addr);
	}

	@Override
	public String toString() {
		return String.format("proj=%s, site=%s, del=%s%s, addr=%s",
				getProjId(), getSiteName(), getProjDelFlag(), getSiteDelFlag(),
				getRef().getAddress()
				);
	}

}
