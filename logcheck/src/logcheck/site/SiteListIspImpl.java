package logcheck.site;

import logcheck.isp.IspList;

public class SiteListIspImpl extends IspList implements SiteListIsp {

	private final String siteName;
	private final String siteDelFlag;
	private final String projDelFlag;

	public SiteListIspImpl(String siteId, String siteName, String siteDelFlag, String projId, String projDelFlag) {
		super(siteId, projId);
		this.siteName = siteName;
		this.siteDelFlag = siteDelFlag;
		this.projDelFlag = projDelFlag;
	}

	@Override
	public String getSiteId() {
		return super.getName();
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
		return super.getCountry();
	}
	@Override
	public String getProjDelFlag() {
		return projDelFlag;
	}
	@Override
	public String getCountry() {
		return "利用申請";
	}
	@Override
	public String getName() {
		return getSiteName();
	}

	@Override
	public String toString() {
		return String.format("siteId=%s, projId=%s, del=%s%s, addrs=[%s]", 
				getSiteId(), getProjId(), getSiteDelFlag(), getProjDelFlag(), getAddress());
	}
}
