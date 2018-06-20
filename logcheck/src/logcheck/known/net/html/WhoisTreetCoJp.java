package logcheck.known.net.html;

import java.io.IOException;
import java.util.logging.Level;

import logcheck.known.KnownListIsp;
import logcheck.known.net.Whois;
import logcheck.util.net.NetAddr;

public class WhoisTreetCoJp extends WhoisHtmlParser implements Whois {

	/*
	 * 引数のIPアドレスを含むISPを取得する
	 */
	@Override
	public KnownListIsp get(NetAddr addr) {
		try {
			return search("http://whois.threet.co.jp/?key=", addr);
		}
		catch (IOException e) {
			log.log(Level.WARNING, e.getMessage());
		}
		return null;
	}

}
