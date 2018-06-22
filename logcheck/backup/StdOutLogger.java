package logcheck.util.log;

import java.io.OutputStream;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

public class StdOutLogger extends Logger {

	private static final String NAME = "log.StdOutHandler";

	protected StdOutLogger() {
		super(NAME, null);
	}

	// Loggerクラスのインスタンスを生成
	public static Logger getLogger() {
		Logger logger = Logger.getLogger(NAME);
		// 標準出力へログを出力するハンドラを追加
		logger.addHandler(new StreamHandler(getOutputStream(), new PlainFormatter()));
		// ルートロガーのハンドラ（ConsoleHandler）へログメッセージを発行しない
		logger.setUseParentHandlers(false);
		// ログレベル設定
		//logger.setLevel(Level.ALL);		
		return logger;
	}

	public static OutputStream getOutputStream() {
		return System.out;
	}

}
