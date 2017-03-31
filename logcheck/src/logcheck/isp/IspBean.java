package logcheck.isp;

public class IspBean<E> implements Comparable<IspBean<E>> {

	private final String name;
	private final String country;
	private final E ref;

	public IspBean(String name, String country, E ref) {
		this.name = name;
		this.country = country;
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

	public String toString() {
		return name + ("".equals(country) ? "" : " (" + country + ")");
	}
	public String toStringWithAddress() {
		return toString() + " " + ref.toString();
	}

	@Override
	public int compareTo(IspBean<E> o) {
		// TODO Auto-generated method stub
		return name.compareTo(o.getName());
	}
}
