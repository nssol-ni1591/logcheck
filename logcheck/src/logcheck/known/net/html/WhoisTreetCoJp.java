package logcheck.known.net.html;

import java.io.IOException;

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
			KnownListIsp isp = search("http://whois.threet.co.jp/?key=", addr);
			return isp;
		}
		catch (IOException e) { }
		return null;
	}

}
