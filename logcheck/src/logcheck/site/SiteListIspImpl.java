package logcheck.site;

import logcheck.isp.IspList;

public class SiteListIspImpl extends IspList implements SiteListIsp {

	private final String siteId;
	private final String siteName;
	private final String siteDelFlag;
//	private final String projId;
	private final String projDelFlag;

	public SiteListIspImpl(String siteName, String projId) {
		super(projId, "利用申請");
		this.siteId = "";
		this.siteName = siteName;
		// tsvファイルに登録されている拠点は正常とであると仮定する
		this.siteDelFlag = "0";
//		this.projId = projId;
		this.projDelFlag = "0";
	}
	public SiteListIspImpl(String siteId, String siteName, String siteDelFlag, String projId, String projDelFlag) {
		super(projId, "利用申請");
		this.siteId = siteId;
		this.siteName = siteName;
		this.siteDelFlag = siteDelFlag;
//		this.projId = projId;
		this.projDelFlag = projDelFlag;
	}

	@Override
	public String getSiteId() {
//		return super.getName();
		return siteId;
	}
	@Override
	public String getSiteName() {
		return siteName;
	}
	@Override
	public String getSiteDelFlag() {
		return siteDelFlag;
	}
	@Override
	public String getProjId() {
//		return super.getCountry();
//		return projId;
		return super.getName();
	}
	@Override
	public String getProjDelFlag() {
		return projDelFlag;
	}
	@Override
	public String getCountry() {
//		return "利用申請";
		return super.getCountry();
	}
	@Override
	public String getName() {
//		return getSiteName();
		return super.getName();
	}

	@Override
	public String toString() {
		return String.format("siteId=%s, projId=%s, del=%s%s, addrs=[%s]", 
				getSiteId(), getProjId(), getSiteDelFlag(), getProjDelFlag(), getAddress());
	}
}
