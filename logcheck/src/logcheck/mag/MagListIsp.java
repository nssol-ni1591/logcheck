package logcheck.mag;

import logcheck.isp.IspList;

public class MagListIsp extends IspList {

	private final String projName;
	private final String siteName;
	// 追加：未利用ユーザ検索
	private final String projDelFlag;
	private final String siteDelFlag;

	public MagListIsp(MagListBean b) {
		super(b.getProjId(), "利用申請");
		this.projName = b.getProjName();
		this.siteName = b.getSiteName();
		this.projDelFlag = b.getProjDelFlag();
		this.siteDelFlag = b.getSiteDelFlag();
	}

	public String getProjId() {
		return super.getName();
	}
	public String getProjName() {
		return projName;
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

}
