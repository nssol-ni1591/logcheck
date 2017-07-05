package logcheck.site;

import java.util.Set;

import logcheck.isp.IspList;
import logcheck.util.net.NetAddr;

public class SiteListKnownIsp implements SiteListIsp {

	private final IspList isp;

	public SiteListKnownIsp(IspList isp) {
		this.isp = isp;
	}

	@Override
	public String getProjId() {
		return isp.getName();
	}
	@Override
	public String getCountry() {
		return isp.getCountry();
	}
	@Override
	public String getName() {
		return isp.getName();
	}
	@Override
	public Set<NetAddr> getAddress() {
		return isp.getAddress();
	}
	@Override
	public void addAddress(String addr) {
		isp.addAddress(addr);
	}

	public String toString() {
		return String.format("siteId=%s, projId=%s, del=%s%s", getSiteId(), getProjId(), getSiteDelFlag(), getProjDelFlag());
	}

}
