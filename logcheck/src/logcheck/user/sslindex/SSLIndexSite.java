package logcheck.user.sslindex;

import logcheck.site.SiteListIsp;
import logcheck.util.Summary;
import logcheck.util.net.NetAddr;

public class SSLIndexSite extends Summary<SiteListIsp> /*implements Summary<String>*/ {

//	private final IspList isp;

//	private String firstDate = "";
//	private String lastDate = "";
//	private int count;

	public SSLIndexSite(SiteListIsp isp) {
		super(isp);
//		this.isp = isp;
//		this.count = 0;
	}

	public SiteListIsp getIsp() {
//		return isp;
		return getRef();
	}

//	public String getFirstDate() {
//		return firstDate;
//	}
//	public String getLastDate() {
//		return lastDate;
//	}
//	@Override
//	public int getCount() {
//		return count;
//	}

	public String getCountry() {
		return getRef().getCountry();
	}
	public String getProjId() {
		return getRef().getName();
	}
	public String getSiteName() {
//		return getRef() instanceof MagListIsp ? ((MagListIsp)getRef()).getSiteName() : "-";
		return getRef().getSiteName();
	}
	public String getProjDelFlag() {
//		return getRef() instanceof MagListIsp ? ((MagListIsp)getRef()).getProjDelFlag() : "-1";
		return getRef().getProjDelFlag();
	}
	public String getSiteDelFlag() {
//		return getRef() instanceof MagListIsp ? ((MagListIsp)getRef()).getSiteDelFlag() : "-1";
		return getRef().getSiteDelFlag();
	}

	public boolean within(NetAddr addr) {
//		return getRef().getAddress().stream().anyMatch(net -> net.within(addr));
		return getRef().within(addr);
	}
//	public void update(String date) {
//		lastDate = date;
//		if ("".equals(firstDate)) {
//			firstDate = date;
//		}
//		count += 1;
//	}
	public boolean isDelFlag() {
		if (!"0".equals(getProjDelFlag())) {
			return true;
		}
		if (!"0".equals(getSiteDelFlag())) {
			return true;
		}
		return false;
	}

	public String toString() {
		return String.format("proj=%s site=%s, del=%s%s addr=%s",
				getProjId(), getSiteName(), getProjDelFlag(), getSiteDelFlag(), getRef().getAddress());
	}

}
