package logcheck.mag;

import java.util.Optional;

import javax.inject.Inject;

import logcheck.site.SiteList;
import logcheck.site.SiteListIsp;
import logcheck.util.net.NetAddr;

public class MagListImpl implements MagList {

	@Inject private SiteList sitelist;
	
	private MagListImpl() {
	}

	@Override
	public MagList load(String file) throws Exception {
		// TODO Auto-generated method stub
		return sitelist.load(file);
	}

	@Override
	public MagListIsp get(NetAddr addr) {
		Optional<SiteListIsp> rc = sitelist.values().stream()
				.filter(isp -> isp.getAddress().stream().anyMatch(net -> net.within(addr)))
				.findFirst();
		return rc.isPresent() ? (MagListIsp)rc.get() : null;
	}

}
