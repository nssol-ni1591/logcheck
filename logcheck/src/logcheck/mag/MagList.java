package logcheck.mag;

import logcheck.util.net.NetAddr;

public interface MagList {

	public MagListIsp get(NetAddr addr);

	public MagList load(String file) throws Exception;

	default public MagList load() throws Exception {
		throw new IllegalArgumentException("not override");
	}

}
