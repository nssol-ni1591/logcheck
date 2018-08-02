package logcheck.known.impl.net;

import logcheck.known.impl.Whois;

public class WhoisTreetCoJp extends WhoisHtmlParser implements Whois {

	public WhoisTreetCoJp() {
		super("http://whois.threet.co.jp/?key=");
	}

	@Override
	public String getName() {
		return "Treet";
	}

}
