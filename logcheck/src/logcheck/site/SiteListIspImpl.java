package logcheck.site;

import logcheck.isp.IspListImpl;

public class SiteListIspImpl extends IspListImpl implements SiteListIsp {

	private final String siteId;
	private final String siteName;
	private final String siteDelFlag;
	private final String projDelFlag;

	// for TsvSiteList
	public SiteListIspImpl(String siteName, String projId) {
		super(projId, "利用申請");
		this.siteId = "";
		this.siteName = siteName;
		// tsvファイルに登録されている拠点は正常とであると仮定する
		this.siteDelFlag = "0";
		this.projDelFlag = "0";
	}
	public SiteListIspImpl(String siteId, String siteName, String siteDelFlag, String projId, String projDelFlag) {
		super(projId, "利用申請");
		this.siteId = siteId;
		this.siteName = siteName;
		this.siteDelFlag = siteDelFlag;
		this.projDelFlag = projDelFlag;
	}
	public SiteListIspImpl(SiteListIsp site, String projId) {
		super(projId, "利用申請");
		this.siteId = site.getSiteId();
		this.siteName = "*" + site.getSiteName();	//ログ解析時のクラス生成であることを示すため
		this.siteDelFlag = site.getSiteDelFlag();
		this.projDelFlag = site.getProjDelFlag();
	}

	@Override
	public String getSiteId() {
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
		return super.getName();
	}
	@Override
	public String getProjDelFlag() {
		return projDelFlag;
	}
	@Override
	public String getCountry() {
		return super.getCountry();
	}
	@Override
	public String getName() {
		return super.getName();
	}

	@Override
	public String toString() {
		return String.format("siteId=%s, projId=%s, del=%s%s, addrs=[%s]", 
				getSiteId(), getProjId(), getSiteDelFlag(), getProjDelFlag(), getAddress());
	}
}
