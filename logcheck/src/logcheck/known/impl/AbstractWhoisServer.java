package logcheck.known.impl;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import logcheck.annotations.WithElaps;
import logcheck.known.KnownListIsp;
import logcheck.util.NetAddr;

public abstract class AbstractWhoisServer implements Callable<KnownListIsp> {

	@Inject protected Logger log;

	protected final String url;
	protected NetAddr addr;

	public AbstractWhoisServer(String url) {
		this.url = url;
	}

	// implement Whois
	public void init() {
		if (log == null) {
			// JUnitの場合、logのインスタンスが生成できないため
			log = Logger.getLogger(this.getClass().getName());
		}
	}

	/*
	 * 引数のIPアドレスを含むISPを取得する
	 * for WhoisKnownListTest (JUnit)
	 */
	@WithElaps
	public KnownListIsp get(NetAddr addr) {
		try {
			return search(url, addr);
		}
		catch (IOException e) {
			log.log(Level.WARNING, e.getMessage());
		}
		return null;
	}

	/*
	 * ISP検索に使用するIPアドレスを設定する
	 */
	public void setAddr(NetAddr addr) {
		this.addr = addr;
	}

	/*
	 * WHoisサーバの検索にスレッドを使用する場合のメソッド
	 * setAddr(NetAddr)と組み合わせて使用する
	 * @see java.util.concurrent.Callable#call()
	 */
	@WithElaps
	public KnownListIsp call() throws Exception {
		try {
			KnownListIsp isp = search(url, addr);

			// 実行結果の確認
			// ExecutorService.invokeAnyでは結果がエラーの場合の処理を例外で受け取るしかないため
			if (isp.getName() == null) {
				throw new IllegalArgumentException("isp.getName() == null");
			}
			if (isp.getCountry() == null) {
				throw new IllegalArgumentException("isp.getCountry() == null");
			}
			if (isp.getAddress().isEmpty()) {
				throw new IllegalArgumentException("isp.getAddress().isEmpty()");
			}
			return isp;
		}
		catch (IOException e) {
			log.log(Level.WARNING, e.getMessage());
			throw e;
		}
	}

	public abstract KnownListIsp search(String site, NetAddr addr) throws IOException;

}
