package logcheck.test;


public class WeldWrapperTestSub2 extends WeldWrapperTestSub {

	@Override
	public boolean check(int argc, String... argv) {
		if ("false".equals(argv[0])) {
			return false;
		}
		return true;
	}

}
