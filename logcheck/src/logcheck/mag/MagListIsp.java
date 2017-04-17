package logcheck.mag;

import logcheck.isp.IspList;

public class MagListIsp extends IspList {

	public MagListIsp(String prjId) {
		super(prjId, "利用申請");
	}

	public String getPrjId() {
		return getName();
	}
}
