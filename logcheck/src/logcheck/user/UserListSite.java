package logcheck.user;

public class UserListSite {

	private final String prjId;
	private final String siteName;
	private final String siteCd;
	private final String connCd;
	

	public UserListSite(String prjId, String siteName, String siteCd, String connCd) {
		this.prjId = prjId;
		this.siteName = siteName;
		this.siteCd = siteCd;
		this.connCd = connCd;
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

	public boolean equals(UserListSite prj) {
		return prjId.equals(prj.prjId);
	}

	public String toString() {
		return String.format("prjId=%s siteName=%s, siteCd=%s connCd=%s", prjId, siteName, siteCd, connCd);
	}

}
