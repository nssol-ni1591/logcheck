package logcheck.known;

public class KnownListBean {

	private final String addr;
	private final String name;
	private final String country;

	public KnownListBean(String addr, String name, String country) {
		this.addr = addr;
		this.name = name;
		this.country = country;
		// コンストラクタで＠Injectを参照することができない
		//log.fine(this.toString());
	}

	public String getAddr() {
		return addr;
	}
	public String getName() {
		return name;
	}
	public String getCountry() {
		return country;
	}

	public String toString() {
		return String.format("addr=%s, name=%s, country=%s", addr, name, country);
	}
}
