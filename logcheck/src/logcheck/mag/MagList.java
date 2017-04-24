package logcheck.mag;

import logcheck.util.NetAddr;

public interface MagList {

	public MagListIsp get(NetAddr addr);

	public MagList load(String file) throws Exception;

//	public MagListBean parse(String s);

//	public boolean test(String s);

}
