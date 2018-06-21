package logcheck.mag;

import java.io.IOException;
import java.sql.SQLException;

import logcheck.isp.IspList;
import logcheck.util.net.NetAddr;

public interface MagList {

	MagList load(String file) throws IOException, ClassNotFoundException, SQLException;

	// MagListには必須だが、SiteListには必須ではないので、MagListに定義する
	IspList get(NetAddr addr);

}
