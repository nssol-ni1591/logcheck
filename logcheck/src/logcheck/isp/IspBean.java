package logcheck.isp;

import java.util.Objects;

public class IspBean<E> implements Isp {

	private final String name;
	private final String country;
	private final E ref;

	public IspBean(String name, String country, E ref) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(country);

		this.name = name;
		this.country = country.length() != 2 ? country : country.toUpperCase();
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
