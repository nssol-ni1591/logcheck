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
		int rc = country.compareTo(o.getCountry());
		if (rc != 0) {
			return rc;
		}
		return name.compareTo(o.getName());
	}

	@Override
	public String toString() {
		return name;
	}

}
