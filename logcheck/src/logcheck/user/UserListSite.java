package logcheck.user;

import java.util.Set;

import logcheck.isp.IspList;
import logcheck.mag.MagListIsp;
import logcheck.site.SiteListBean;
import logcheck.site.SiteListKnownIsp;
import logcheck.site.SiteListMagIsp;
import logcheck.util.Summary;
import logcheck.util.net.NetAddr;

public class UserListSite extends Summary<SiteListBean> {

	public UserListSite(SiteListBean site) {
		super(site);
	}
	public UserListSite(MagListIsp isp) {
		super(new SiteListMagIsp(isp));
	}
	public UserListSite(IspList isp) {
		super(new SiteListKnownIsp(isp));
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
	public void addAddress(String addr) {
		getRef().addAddress(addr);
	}
	public boolean within(NetAddr addr) {
		return getRef().getAddress().stream().anyMatch(net -> net.within(addr));
	}

	@Override
	public String toString() {
		return String.format("proj=%s, site=%s, del=%s%s, addr=%s",
				getProjId(), getSiteName(), getProjDelFlag(), getSiteDelFlag(),
				getRef().getAddress()
				);
	}

}
