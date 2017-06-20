package logcheck.user;

import logcheck.isp.IspList;
import logcheck.mag.MagListIsp;
import logcheck.util.Summary;
import logcheck.util.net.NetAddr;

public class UserListSummary extends IspList implements Summary {

	private final String siteName;
	private final String projDelFlag;
	private final String siteDelFlag;

	private String firstDate = "";
	private String lastDate = "";
	private int count;

	public UserListSummary(String projId, String siteName, NetAddr siteAddr, String projDelFlag, String siteDelFlag) {
		super(projId, "利用申請");
		this.siteName = siteName;
		this.projDelFlag = projDelFlag;
		this.siteDelFlag = siteDelFlag;
		super.addAddress(siteAddr);
	}
	public UserListSummary(IspList isp) {
		super(isp.getName(), isp.getCountry());
		this.siteName = "-";
		this.projDelFlag = "-";
		this.siteDelFlag = "-";
		isp.getAddress().forEach(addr -> addAddress(addr));
	}
	public UserListSummary(MagListIsp isp) {
		super(isp.getProjId(), "利用申請");
		this.siteName = isp.getSiteName();
		this.projDelFlag = "-";
		this.siteDelFlag = "-";
		isp.getAddress().forEach(addr -> addAddress(addr));
	}

	public String getCountry() {
		return super.getCountry();
	}
	public String getProjId() {
		return super.getName();
	}
	public String getSiteName() {
		return siteName;
	}
	public String getProjDelFlag() {
		return projDelFlag;
	}
	public String getSiteDelFlag() {
		return siteDelFlag;
	}
	public boolean isDelFlag() {
		if (!projDelFlag.equals("0")) {
			return true;
		}
		if (!siteDelFlag.equals("0")) {
			return true;
		}
		return false;
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
	public void update(String date) {
		lastDate = date;
		if ("".equals(firstDate)) {
			firstDate = date;
		}
		count += 1;
	}

	public String toString() {
		return String.format("proj=%s site=%s, del=%s%s addr=%s", getProjId(), getSiteName(), projDelFlag, siteDelFlag, getAddress());
	}

}
