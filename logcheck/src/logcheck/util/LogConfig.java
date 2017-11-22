package logcheck.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LogConfig {

	private static final Logger log = Logger.getLogger(LogConfig.class.getName());

	public LogConfig() {
		try {
			LogManager.getLogManager().readConfiguration(getClass().getResourceAsStream("/META-INF/logging.properties"));
			// このクラスと同じパッケージでは無い場合は /myapp/logging.properties など絶対パス指定
		} catch (IOException e) {
			log.log(Level.SEVERE, "例外", e);
		}
	}

}
