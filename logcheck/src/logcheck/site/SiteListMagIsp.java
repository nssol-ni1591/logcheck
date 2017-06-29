package logcheck.site;

import java.util.Set;

import logcheck.mag.MagListIsp;
import logcheck.util.net.NetAddr;

public class SiteListMagIsp implements SiteListIsp {

	private final MagListIsp isp;

	public SiteListMagIsp(MagListIsp isp) {
		this.isp = isp;
	}

	@Override
	public String getSiteName() {
		return isp.getSiteName();
	}
	@Override
	public String getSiteDelFlag() {
		return isp.getSiteDelFlag();
	}
	@Override
	public String getProjId() {
		return isp.getProjId();
	}
	@Override
	public String getProjDelFlag() {
		return isp.getProjDelFlag();
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
	public 	Set<NetAddr> getAddress() {
		return isp.getAddress();
	}
	@Override
	public void addAddress(String addr) {
		isp.addAddress(addr);
	}

	@Override
	public String toString() {
		return String.format("siteId=%s, projId=%s, del=%s%s", getSiteId(), getProjId(), getSiteDelFlag(), getProjDelFlag());
	}
}
