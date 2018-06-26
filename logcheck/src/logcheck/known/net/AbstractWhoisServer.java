package logcheck.known.net;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import logcheck.annotations.WithElaps;
import logcheck.known.KnownListIsp;
import logcheck.util.net.NetAddr;

public abstract class AbstractWhoisServer implements Whois {

	@Inject protected transient Logger log;

	protected final String url;
	
	public AbstractWhoisServer(String url) {
		this.url = url;
	}

	@Override
	public void init() {
		if (log == null) {
			// JUnitの場合、logのインスタンスが生成できないため
			log = Logger.getLogger(this.getClass().getName());
		}
	}

	/*
	 * 引数のIPアドレスを含むISPを取得する
	 */
	@Override @WithElaps
	public KnownListIsp get(NetAddr addr) {
		try {
			return search(url, addr);
		}
		catch (IOException e) {
			log.log(Level.WARNING, e.getMessage());
		}
		return null;
	}

	public abstract KnownListIsp search(String site, NetAddr addr) throws IOException;

}
