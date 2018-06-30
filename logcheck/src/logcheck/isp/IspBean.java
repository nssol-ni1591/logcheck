package logcheck.isp;

public class IspBean<E> implements Comparable<IspBean<E>> {

	private final String name;
	private final String country;
	private final E ref;

	public IspBean(String name, String country, E ref) {
		this.name = name;
		this.country = country == null ? null : country.toUpperCase();
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
	public int compareTo(IspBean<E> o) {
		if (o == null) {
			return -1;
		}

		int rc = country.compareTo(o.getCountry());
		if (rc != 0) {
			return rc;
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
