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
	public int compareTo(Isp o) {
		if (o == null) {
			return -1;
		}

		if (country == null) {
			if (o.getCountry() != null) {
				return 1;
			}
		}
		else {
			int rc = country.compareTo(o.getCountry());
			if (rc != 0) {
				return rc;
			}
		}
		if (name == null) {
			if (o.getName() != null) {
				return 1;
			}
			else {
				return 0;
			}
		}
		return name.compareTo(o.getName());
	}
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o instanceof IspBean<?>) {
			@SuppressWarnings("unchecked")
			IspBean<E> b = (IspBean<E>)o;
			return compareTo(b) == 0;
		}
		return false;
	}

	@Override
	public String toString() {
		return name;
	}

}
