package logcheck.site;

import java.util.Set;

import logcheck.isp.IspList;
import logcheck.util.net.NetAddr;

public interface SiteListIsp extends IspList {

	default String getSiteId() {
		return null;
	}
	default String getSiteName() {
		return "-";
	}
	String getSiteDelFlag();
	String getProjId();
	String getProjDelFlag();

//	default String getSiteDelFlag() {
//		return "-1";
//	}
//	default String getProjId() {
//		return "-";
//	}
//	default String getProjDelFlag() {
//		return "-1";
//	}

//	default String getCountry() {
//		return "-";
//	}
//	default String getName() {
//		return "-";
//	}

	default Set<NetAddr> getAddress() {
		throw new IllegalArgumentException("don't use");
	}
	default void addAddress(NetAddr addr) {
		throw new IllegalArgumentException("don't use");
	}

}
