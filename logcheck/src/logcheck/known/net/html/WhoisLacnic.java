package logcheck.known.net.html;

import logcheck.known.net.Whois;

public class WhoisLacnic extends WhoisHtmlParser implements Whois {

	public WhoisLacnic() {
		super("http://whois.threet.co.jp/?key=");
	}

}
