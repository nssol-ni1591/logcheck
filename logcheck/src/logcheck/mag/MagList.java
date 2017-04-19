package logcheck.mag;

import java.io.IOException;

import logcheck.util.NetAddr;

public interface MagList {

	public MagListIsp get(NetAddr addr);

	public MagList load(String file) throws IOException;

	public MagListBean parse(String s);

	public boolean test(String s);

}
