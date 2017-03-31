package logcheck.mag;

import logcheck.isp.IspList;

public class MagListIsp extends IspList {

	public MagListIsp(String prjId) {
		super(prjId);
	}

	public String getPrjId() {
		return getName();
	}
}
