package logcheck.util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public interface WeldRunner {

	void init(String...argv) throws IOException, ClassNotFoundException, SQLException;

	int start(String[] argv, int argc)
			throws InterruptedException, ExecutionException, IOException;

	default String usage(String name) {
		return String.format("usage: java %s knownlist maglist [accesslog...]", name);
	}

	default boolean check(int argc, String...argv) {
		return true;
	}

}
