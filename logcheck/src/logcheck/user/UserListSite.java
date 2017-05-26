package logcheck.user;

import logcheck.isp.IspList;
import logcheck.mag.MagListIsp;
import logcheck.util.NetAddr;

public class UserListSite extends IspList {

//	private final String prjId;
//	private final String siteName;
//	private final String siteCd;
//	private final String connCd;
	private final String projDelFlag;
	private final String siteDelFlag;

	private String firstDate = "";
	private String lastDate = "";
	private int count;

//	public UserListSite(String prjId, String siteName, String siteIp, String siteCd, String connCd, String prjDelFlag, String siteDelFlag) {
	public UserListSite(String projId, String siteName, NetAddr siteAddr, String projDelFlag, String siteDelFlag) {
		super(siteName, projId);
//		this.prjId = prjId;
//		this.siteName = siteName;
//		this.siteCd = siteCd;
//		this.connCd = connCd;
		this.projDelFlag = projDelFlag;
		this.siteDelFlag = siteDelFlag;
		super.addAddress(siteAddr);
	}
	public UserListSite(IspList isp) {
		super(isp.getName(), isp.getCountry());
		this.projDelFlag = "-";
		this.siteDelFlag = "-";
		isp.getAddress().forEach(addr -> addAddress(addr));
	}
	public UserListSite(MagListIsp isp) {
		super(isp.getName(), isp.getProjId());
		this.projDelFlag = "-";
		this.siteDelFlag = "-";
		isp.getAddress().forEach(addr -> addAddress(addr));
	}

	public String getProjId() {
//		return prjId;
		return super.getCountry();
	}
	public String getSiteName() {
//		return siteName;
		return super.getName();
	}
	/*
	public String getSiteCd() {
		return siteCd;
	}
	public String getConnCd() {
		return connCd;
	}
	*/
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
		//return String.format("prjId=%s site=%s, code=[%s, %s], del=[%s, %s]", prjId, siteName, siteCd, connCd, prjDelFlag, siteDelFlag);
		return String.format("projId=%s site=%s, del=%s%s addr=%s", getProjId(), getSiteName(), projDelFlag, siteDelFlag, getAddress());
	}

}
