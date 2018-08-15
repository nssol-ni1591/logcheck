package logcheck.known.impl.net;

import java.util.LinkedHashSet;
import java.util.Optional;

import logcheck.annotations.WithElaps;
import logcheck.known.KnownList;
import logcheck.known.KnownListIsp;
import logcheck.util.Constants;
import logcheck.util.NetAddr;

/*
 * 既知のISPのIPアドレスを取得する
 * 取得先は、引数に指定された「既知ISP_IPアドレス一覧」ファイル
 * 
 * 問題点：
 * 広いアドレス空間をISPが取得し、その一部を企業に貸し出しているよう場合、
 * IPアドレスから取得される接続元はISP名ではなく企業名を取得したい。
 * 今のHashMapでは、Hash地の値により、どちらが取得されるか判断付かない。
 */
public class PrivateAddrList extends LinkedHashSet<KnownListIsp> implements KnownList {

	private static final long serialVersionUID = 1L;
	private static final String PRIVATE = "プライベート";

	public PrivateAddrList() {
		super();
	}

	// for envoronment not using weld-se
	public void init() {
		// Do nothing
	}

	/*
	 * 引数のIPアドレスを含むISPを取得する
	 */
	public KnownListIsp get(NetAddr addr) {
		Optional<KnownListIsp> rc = this.stream()
				.filter(isp -> isp.within(addr))
				.findFirst();
		//return rc.isPresent() ? rc.get() : null
		return rc.isPresent() ? rc.get() : new KnownListIsp(addr.toString(), Constants.UNKNOWN_COUNTRY);
	}

	@WithElaps
	public KnownList load(String file) {
		KnownListIsp isp;
		isp = new KnownListIsp("LOOPBACK-RESERVED", PRIVATE);
		isp.addAddress(new NetAddr(Constants.LOCALHOST));
		add(isp);
		isp = new KnownListIsp("ABLK-RFC1918-RESERVED", PRIVATE);
		isp.addAddress(new NetAddr(Constants.CLASS_A));
		add(isp);
		isp = new KnownListIsp("BBLK-RFC1918-RESERVED", PRIVATE);
		isp.addAddress(new NetAddr(Constants.CLASS_B));
		add(isp);
		isp = new KnownListIsp("CBLK-RFC1918-RESERVED", PRIVATE);
		isp.addAddress(new NetAddr(Constants.CLASS_C));
		add(isp);
		return this;
	}

}
