package logcheck.site;

import java.util.Set;

import logcheck.isp.IspList;
import logcheck.mag.MagListIsp;
import logcheck.util.net.NetAddr;

public class SiteListMagIsp implements SiteListIsp {

//	private final MagListIsp isp;
	private final SiteListIsp isp;

	public SiteListMagIsp(MagListIsp isp) {
		this.isp = isp;
	}
	public SiteListMagIsp(IspList isp) {
		this.isp = (SiteListIsp)isp;
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
//		return isp.getProjId();
		return isp.getName();
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

}
