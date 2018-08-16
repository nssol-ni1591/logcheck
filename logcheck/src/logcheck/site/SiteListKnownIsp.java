package logcheck.site;

import java.util.Set;

import logcheck.isp.Isp;
import logcheck.isp.IspList;
import logcheck.util.NetAddr;

public class SiteListKnownIsp implements SiteListIsp {

	private final IspList isp;

	public SiteListKnownIsp(IspList isp) {
		this.isp = isp;
	}

	@Override
	public String getProjId() {
		return isp.getName();
	}
	@Override
	public String getSiteName() {
		return "*ISP経由接続";
	}
	@Override
	public String getCountry() {
		return isp.getCountry();
	}
	@Override
	public String getName() {
		return isp.getName();
	}
	@Override
	public Set<NetAddr> getAddress() {
		return isp.getAddress();
	}
	@Override
	public void addAddress(NetAddr addr) {
		isp.addAddress(addr);
	}
	@Override
	public String getSiteDelFlag() {
		return "0";
	}
	@Override
	public String getProjDelFlag() {
		return "0";
	}
	@Override
	public String getSiteId() {
		// 利用申請ファイル(Excel)には該当する属性は存在しないため
		throw new IllegalArgumentException("not impliment");
	}


	@Override
	public int compareTo(Isp o) {
		return isp.compareTo(o);
	}

}
