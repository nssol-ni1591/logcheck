package logcheck.known.impl.net;

import logcheck.known.impl.Whois;

public class WhoisLacnic extends WhoisHtmlParser implements Whois {

	public WhoisLacnic() {
		super("http://lacnic.net/cgi-bin/lacnic/whois?lg=EN&query=");
	}

	@Override
	public String getName() {
		return "Lacnic";
	}

}
