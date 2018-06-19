package logcheck.site;

import logcheck.isp.IspList;

public interface SiteListIsp extends IspList {

	default String getSiteId() {
		return null;
	}

	String getSiteName();
	String getSiteDelFlag();
	String getProjId();
	String getProjDelFlag();

}
