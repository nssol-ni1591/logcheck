package logcheck.user;

public class UserListSite {

	private final String prjId;
	private final String siteName;
	private final String siteCd;
	private final String connCd;
	private final String prjDelFlag;
	private final String siteDelFlag;

	public UserListSite(String prjId, String siteName, String siteCd, String connCd, String prjDelFlag, String siteDelFlag) {
		this.prjId = prjId;
		this.siteName = siteName;
		this.siteCd = siteCd;
		this.connCd = connCd;
		this.prjDelFlag = prjDelFlag;
		this.siteDelFlag = siteDelFlag;
	}

	public String getPrjId() {
		return prjId;
	}
	public String getSiteName() {
		return siteName;
	}
	public String getSiteCd() {
		return siteCd;
	}
	public String getConnCd() {
		return connCd;
	}
	public String getPrjDelFlag() {
		return prjDelFlag;
	}
	public String getSiteDelFlag() {
		return siteDelFlag;
	}

	public boolean equals(UserListSite prj) {
		return prjId.equals(prj.prjId);
	}

	public String toString() {
		return String.format("prjId=%s site=%s, code=[%s, %s], del=[%s, %s]", prjId, siteName, siteCd, connCd, prjDelFlag, siteDelFlag);
	}

}
