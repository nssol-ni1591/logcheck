package logcheck.mag;

import logcheck.isp.IspList;

public class MagListIsp extends IspList {

	private final String projName;
	private final String siteName;
/*
	public MagListIsp(String projId) {
		super(projId, "利用申請");
		this.siteName = "";
	}
*/
	public MagListIsp(MagListBean b) {
		super(b.getProjId(), "利用申請");
		this.projName = b.getProjName();
		this.siteName = b.getSiteName();
	}
/*
	public MagListIsp(String projId, String siteName) {
		super(projId, "利用申請");
		this.projName = "";
		this.siteName = siteName;
	}
*/
	public String getProjId() {
		return super.getName();
	}
	public String getProjName() {
		return projName;
	}
	public String getSiteName() {
		return siteName;
	}

}
