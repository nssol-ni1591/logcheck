package logcheck.user;

import java.util.Set;

import logcheck.isp.IspList;
import logcheck.mag.MagListIsp;
import logcheck.site.SiteListBean;
import logcheck.site.SiteListKnownIsp;
import logcheck.site.SiteListMagIsp;
import logcheck.util.Summary;
import logcheck.util.net.NetAddr;

public class UserListSummary implements Summary<String> {

	private final SiteListBean site;

	private String firstDate = "";
	private String lastDate = "";
	private int count = 0;

	public UserListSummary(SiteListBean site) {
		this.site = site;
	}
	public UserListSummary(MagListIsp isp) {
		this.site = new SiteListMagIsp(isp);
	}
	public UserListSummary(IspList isp) {
		this.site = new SiteListKnownIsp(isp);
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
		return "利用申請";
	}
	public String getProjId() {
//		return site == null ? null : site.getProjId();
		return site.getProjId();
	}
	public String getSiteId() {
		return site.getSiteId();
	}
	public String getSiteName() {
//		return site == null ? null : site.getSiteName();
		return site.getSiteName();
	}
	public String getProjDelFlag() {
//		return site == null ? "_" : site.getProjDelFlag();
		return site.getProjDelFlag();
	}
	public String getSiteDelFlag() {
//		return site == null ? "_" : site.getSiteDelFlag();
		return site.getSiteDelFlag();
	}

	public Set<NetAddr> getAddress() {
//		return site == null ? new HashSet<>() : site.getAddress();
		return site.getAddress();
	}
	public void addAddress(String addr) {
		site.addAddress(addr);
	}
	public boolean within(NetAddr addr) {
//		return site == null ? false : site.getAddress().stream().anyMatch(net -> net.within(addr));
		return site.getAddress().stream().anyMatch(net -> net.within(addr));
	}
	public void update(String date) {
		lastDate = date;
		if ("".equals(firstDate)) {
			firstDate = date;
		}
		count += 1;
	}

	@Override
	public String toString() {
		return String.format("proj=%s, site=%s, del=%s%s, addr=%s",
				getProjId(), getSiteName(), getProjDelFlag(), getSiteDelFlag(),
//				site == null ? "[]" : site.getAddress()
				site.getAddress()
				);
	}

}
