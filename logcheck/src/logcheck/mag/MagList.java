package logcheck.mag;

import logcheck.isp.IspList;
import logcheck.util.net.NetAddr;

public interface MagList {

//	default MagList load() throws Exception {
//		return null;
//	}

	MagList load(String file) throws Exception;

	// MagListには必須だが、SiteListには必須ではないので
	IspList get(NetAddr addr);
//	default MagListIsp get(NetAddr addr) {
//		return null;
//	}

}
