package logcheck.site;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import logcheck.mag.MagList;
import logcheck.util.NetAddr;

/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
public interface SiteList extends Map<String, SiteListIsp>, MagList {

	SiteList load(String file) throws IOException, ClassNotFoundException, SQLException;

	default SiteListIsp get(NetAddr addr) {
		Optional<SiteListIsp> rc =
				values().stream()
				.filter(isp -> isp.within(addr))
				.findFirst();
		return rc.isPresent() ? rc.get() : null;
	}

}
