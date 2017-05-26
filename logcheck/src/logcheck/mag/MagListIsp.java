package logcheck.mag;

import logcheck.isp.IspList;

public class MagListIsp extends IspList {

	public MagListIsp(String projId) {
		super(projId, "利用申請");
	}

	public String getProjId() {
		return getName();
	}
}
