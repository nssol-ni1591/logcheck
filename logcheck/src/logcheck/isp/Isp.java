package logcheck.isp;

import java.util.Objects;

import logcheck.util.net.NetAddr;

public interface Isp extends Comparable<Isp> {

	String getName();

	String getCountry();

	default String getHostname(NetAddr addr) {
		return addr.toString();
	}

	default int compareTo(Isp o) {
		Objects.requireNonNull(o);

		int rc = Objects.compare(getCountry(), o.getCountry(), (o1, o2) -> o1.compareTo(o2));
		if (rc != 0) {
			return rc;
		}
		rc = Objects.compare(getName(), o.getName(), (o1, o2) -> o1.compareTo(o2));
		if (rc != 0) {
			return rc;
		}
		return 0;
	}

}
