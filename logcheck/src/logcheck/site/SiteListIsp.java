package logcheck.site;

import logcheck.isp.IspList;

public interface SiteListIsp extends IspList {

	String getSiteId();

	String getSiteName();
	String getSiteDelFlag();
	String getProjId();
	String getProjDelFlag();

}
