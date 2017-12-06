package logcheck.site;

import java.util.Set;

import logcheck.isp.IspList;
import logcheck.util.net.NetAddr;

public interface SiteListIsp extends IspList {

	default String getSiteId() {
		return null;
	}

	String getSiteName();
	String getSiteDelFlag();
	String getProjId();
	String getProjDelFlag();

	default Set<NetAddr> getAddress() {
		throw new IllegalArgumentException("don't use");
	}
	default void addAddress(NetAddr addr) {
		throw new IllegalArgumentException("don't use");
	}

}
