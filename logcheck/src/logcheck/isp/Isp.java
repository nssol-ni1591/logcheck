package logcheck.isp;

import logcheck.util.net.NetAddr;

public interface Isp extends Comparable<Isp> {

	String getName();

	String getCountry();

	default String getHostname(NetAddr addr) {
		return addr.toString();
	}

	default int compareTo(Isp o) {
		if (o == null) {
			return -1;
		}

		if (getCountry() == null) {
			if (o.getCountry() != null) {
				return 1;
			}
		}
		else {
			int rc = getCountry().compareTo(o.getCountry());
			if (rc != 0) {
				return rc;
			}
		}
		if (getName() == null) {
			if (o.getName() != null) {
				return 1;
			}
			else {
				return 0;
			}
		}
		return getName().compareTo(o.getName());
	}

}
