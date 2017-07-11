package logcheck.user;

import java.util.Set;

import logcheck.known.KnownListIsp;
import logcheck.site.SiteListIsp;
import logcheck.site.SiteListKnownIsp;
import logcheck.util.Summary;
import logcheck.util.net.NetAddr;

public class UserListSite extends Summary<SiteListIsp> {

	public UserListSite(SiteListIsp site) {
		super(site);
	}
	public UserListSite(KnownListIsp isp) {
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
