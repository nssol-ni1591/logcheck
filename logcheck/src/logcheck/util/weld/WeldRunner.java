package logcheck.util.weld;

public interface WeldRunner {

	void init(String...argv) throws Exception;
	int start(String[] argv, int argc) throws Exception;

	default String usage(String name) {
		return String.format("usage: java %s knownlist maglist [accesslog...]", name);
	}
	default boolean check(int argc, String...argv) {
		return true;
	}

}
