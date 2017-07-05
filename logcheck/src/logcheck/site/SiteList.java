package logcheck.site;

import java.util.Map;
import java.util.Optional;

import logcheck.isp.IspList;
import logcheck.mag.MagList;
import logcheck.util.net.NetAddr;

/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
public interface SiteList extends Map<String, SiteListIsp>, MagList {

	SiteList load(String file) throws Exception;

//	SiteListIsp get(Object siteId);
	default IspList get(NetAddr addr) {
		Optional<SiteListIsp> rc =
				values().stream()
				.filter(isp -> isp.getAddress().stream().anyMatch(net -> net.within(addr)))
				.findFirst();
		return rc.isPresent() ? (IspList)rc.get() : null;
	}

}
