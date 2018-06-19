package logcheck.site.tsv;


public class TsvSiteListBean {

	private final String projId;
	private final String projName;
	private final String siteName;
	private final String magIp;

	public TsvSiteListBean(String projId,
			String projName,
			String siteName,
			String magIp,
			String magMask) {
		this.projId = projId;
		this.projName = projName;
		this.siteName = siteName;
		if (magIp.contains("/")) {
			throw new IllegalArgumentException("magIp contains \"/\"");
		}
		this.magIp = magIp + "/" + magMask;
	}

	public String getProjId() {
		return projId;
	}
	public String getProjName() {
		return projName;
	}
	public String getSiteName() {
		return siteName;
	}
	public String getMagIp() {
		return magIp;
	}

//	public String toString() {
//		return String.format("projId=%s, magIp=%s", projId, magIp);
//	}

}
