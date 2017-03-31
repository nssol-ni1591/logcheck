package logcheck.known;

public class KnownBean {

	private final String name;
	private final String country;

	public KnownBean(String name, String country) {
		this.name = name;
		this.country = country;
		//System.out.println(this);
	}

	public String getName() {
		return name;
	}
	public String getCountry() {
		return country;
	}

	public String toString() {
		return String.format("name=%s, country=%s", name, country);
	}
}
