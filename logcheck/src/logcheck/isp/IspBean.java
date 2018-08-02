package logcheck.isp;

public class IspBean<E> implements Isp {

	private final String name;
	private final String country;
	private final E ref;

	public IspBean(String name, String country, E ref) {
		this.name = name;
		//this.country = country == null ? null : (country.length() == 2 ? country.toUpperCase() : country)
		if (country == null || country.length() != 2) {
			this.country = country;
		}
		else {
			this.country = country.toUpperCase();
		}
		this.ref = ref;
	}

	public String getName() {
		return name;
	}
	public String getCountry() {
		return country;
	}
	protected E getRef() {
		return ref;
	}

	@Override
	public String toString() {
		return name;
	}

}
