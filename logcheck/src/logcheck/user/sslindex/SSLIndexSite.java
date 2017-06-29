package logcheck.user.sslindex;

import logcheck.isp.IspList;
import logcheck.mag.MagListIsp;
import logcheck.util.Summary;
import logcheck.util.net.NetAddr;

public class SSLIndexSite extends Summary<IspList> /*implements Summary<String>*/ {

//	private final IspList isp;

//	private String firstDate = "";
//	private String lastDate = "";
//	private int count;

	public SSLIndexSite(IspList isp) {
		super(isp);
//		this.isp = isp;
//		this.count = 0;
	}

	public IspList getIsp() {
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
		return getRef() instanceof MagListIsp ? ((MagListIsp)getRef()).getSiteName() : "-";
	}
	public String getProjDelFlag() {
		return getRef() instanceof MagListIsp ? ((MagListIsp)getRef()).getProjDelFlag() : "-1";
	}
	public String getSiteDelFlag() {
		return getRef() instanceof MagListIsp ? ((MagListIsp)getRef()).getSiteDelFlag() : "-1";
	}

	public boolean within(NetAddr addr) {
		return getRef().getAddress().stream().anyMatch(net -> net.within(addr));
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
