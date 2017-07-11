package logcheck.mag;

import logcheck.isp.IspList;
import logcheck.util.net.NetAddr;

public interface MagList {

	MagList load(String file) throws Exception;

	// MagListには必須だが、SiteListには必須ではないので、MagListに定義する
	IspList get(NetAddr addr);

}
