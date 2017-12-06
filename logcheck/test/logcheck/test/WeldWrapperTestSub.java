package logcheck.test;

import logcheck.util.weld.WeldRunner;

public class WeldWrapperTestSub implements WeldRunner {

	@Override
	public void init(String...argv) throws Exception {
		// dummy
	}
	@Override
	public int start(String[] argv, int argc) throws Exception {
		return 0;
	}

}
