package logcheck.site;

import java.util.Set;

import logcheck.util.net.NetAddr;

public interface SiteListIsp {

	default String getSiteId() {
		return null;
	}
	default String getSiteName() {
		return "-";
	}
	default String getSiteDelFlag() {
		return "-1";
	}
	default String getProjId() {
		return "-";
	}
	default String getProjDelFlag() {
		return "-1";
	}

	default String getCountry() {
		return "-";
	}
	default String getName() {
		return "-";
	}

	Set<NetAddr> getAddress();

	void addAddress(String addr);

}
