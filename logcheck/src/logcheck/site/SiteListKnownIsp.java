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
	public String getSiteName() {
		return "*ISP経由接続";
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
	public void addAddress(NetAddr addr) {
		isp.addAddress(addr);
	}
	@Override
	public String getSiteDelFlag() {
		return "0";
	}
	@Override
	public String getProjDelFlag() {
		return "0";
	}

	@Override
	public int compareTo(IspList o) {
		if (o == null) {
			return -1;
		}
		int rc = getCountry().compareTo(o.getCountry());
		if (rc != 0) {
			return rc;
		}
		rc = getName().compareTo(o.getName());
		return rc;
	}

}
