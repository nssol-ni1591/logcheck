package logcheck.isp;

import java.util.Objects;

public class IspBean<E> implements Isp {

	private final String name;
	private final String country;
	private final E ref;

	public IspBean(String name, String country, E ref) {
		// whoisサーバの検索に失敗したときなどにnuｌｌが引き渡される可能性あり
		//Objects.requireNonNull(name)
		//Objects.requireNonNull(country)

		this.name = name;
		if (country == null) {
			this.country = null;
		}
		else {
			this.country = country.length() != 2 ? country : country.toUpperCase();
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

	// compareTo()はIspに実装済み
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		Objects.requireNonNull(o);

		if (o instanceof Isp) {
			Isp isp = (Isp) o;
			int rc = this.compareTo(isp);
			if (rc != 0) {
				return false;
			}
			return Objects.equals(getName(), ((Isp) o).getName());
		}
		return false;
	}

}
