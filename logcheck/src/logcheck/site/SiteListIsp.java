package logcheck.site;

import logcheck.isp.IspList;

public interface SiteListIsp extends IspList {

	String getSiteName();
	String getSiteDelFlag();
	String getProjId();
	String getProjDelFlag();
	
	default String getName() {
		// 利用申請ファイル(Excel)には該当する属性は存在しないため
		throw new IllegalArgumentException("not impliment");
	}
	default String getSiteId() {
		// 利用申請ファイル(Excel)には該当する属性は存在しないため
		throw new IllegalArgumentException("not impliment");
	}

}
