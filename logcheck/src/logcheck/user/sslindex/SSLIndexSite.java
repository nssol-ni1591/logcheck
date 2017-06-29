package logcheck.user.sslindex;

import logcheck.isp.IspList;
import logcheck.mag.MagListIsp;
import logcheck.util.Summary;
import logcheck.util.net.NetAddr;

public class SSLIndexSite implements Summary<String> {

	private final IspList isp;

	private String firstDate = "";
	private String lastDate = "";
	private int count;

	public SSLIndexSite(IspList isp) {
		this.isp = isp;
		this.count = 0;
	}

	public IspList getIsp() {
		return isp;
	}

	public String getFirstDate() {
		return firstDate;
	}
	public String getLastDate() {
		return lastDate;
	}
	@Override
	public int getCount() {
		return count;
	}

	public String getCountry() {
		return isp.getCountry();
	}
	public String getProjId() {
		return isp.getName();
	}
	public String getSiteName() {
		return isp instanceof MagListIsp ? ((MagListIsp)isp).getSiteName() : "-";
	}
	public String getProjDelFlag() {
		return isp instanceof MagListIsp ? ((MagListIsp)isp).getProjDelFlag() : "-1";
	}
	public String getSiteDelFlag() {
		return isp instanceof MagListIsp ? ((MagListIsp)isp).getSiteDelFlag() : "-1";
	}

	public boolean within(NetAddr addr) {
		return isp.getAddress().stream().anyMatch(net -> net.within(addr));
	}
	public void update(String date) {
		lastDate = date;
		if ("".equals(firstDate)) {
			firstDate = date;
		}
		count += 1;
	}
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
				getProjId(), getSiteName(), getProjDelFlag(), getSiteDelFlag(), isp.getAddress());
	}

}
