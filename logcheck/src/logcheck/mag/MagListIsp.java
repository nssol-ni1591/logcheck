package logcheck.mag;

import logcheck.AbstractChecker;
import logcheck.isp.IspList;

public class MagListIsp extends IspList {

	public MagListIsp(String prjId) {
		super(prjId, AbstractChecker.MAG);
	}

	public String getPrjId() {
		return getName();
	}
}
