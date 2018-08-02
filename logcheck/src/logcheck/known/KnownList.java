package logcheck.known;

import java.io.IOException;
import java.util.Set;

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
public interface KnownList extends Set<KnownListIsp> {

	KnownListIsp get(NetAddr addr);

	KnownList load(String file) throws IOException;

	// for envoronment not using weld-se
	void init();

	default void store(String file) throws IOException {
		throw new IllegalArgumentException("not impelemnt");
	}

}
