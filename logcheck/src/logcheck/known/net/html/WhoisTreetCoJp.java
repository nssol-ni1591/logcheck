package logcheck.known.net.html;

import logcheck.known.net.Whois;

public class WhoisTreetCoJp extends WhoisHtmlParser implements Whois {

	public WhoisTreetCoJp() {
		super("http://whois.threet.co.jp/?key=");
	}

	@Override
	public String getName() {
		return "Treet";
	}

}
