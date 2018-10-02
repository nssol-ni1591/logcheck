package logcheck.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/*
 * 設定方法：
 * java.util.logging.config.class：設定クラス
 * java.util.logging.config.file：設定ファイル
 */
public class LogConfig {

	private static final Logger log = Logger.getLogger(LogConfig.class.getName());

	public LogConfig() {
		init();
	}

	private void init() {
		try {
			LogManager.getLogManager().readConfiguration(getClass().getResourceAsStream("/META-INF/logging.properties"));
			// このクラスと同じパッケージでは無い場合は /myapp/logging.properties など絶対パス指定
		} catch (IOException e) {
			log.log(Level.SEVERE, "例外", e);
		}
	}

}
