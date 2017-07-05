package logcheck.mag;

import logcheck.site.SiteListIsp;
import logcheck.site.SiteListIspImpl;

/*
 * MagListIspはIspListを継承しないといけないのでinterfaceにすることはできない
 */
public class MagListIsp extends SiteListIspImpl implements SiteListIsp {

	private MagListIsp(String siteName, String projId) {
		super(siteName, projId);
	}

//	public String getName() {
//		return super.getSiteId();
//	}

}
