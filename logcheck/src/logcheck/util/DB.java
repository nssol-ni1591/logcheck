package logcheck.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

@Dependent
public class DB {

	private DB() {
		// Do nothing
	}

	/*
	 * Producerを使うか、try-with-resourceを使うか迷ったけれど、try-with-resourceを使用することにした
	 */
	@Produces
	public static Connection createConnection() throws ClassNotFoundException, SQLException, IOException {
		final Logger log = Logger.getLogger(DB.class.getName());

		Properties props = new Properties();
		InputStream is = DB.class.getResourceAsStream("/META-INF/jdbc.properties");
		if (is != null) {
			props.load(new InputStreamReader(is));
		}

		String username = props.getProperty("username", Constants.MASTERINFO_USER);
		String password = props.getProperty("password", Constants.MASTERINFO_PASS);

		String env = System.getProperty("jdbc.env", "dev");
		String driver = props.getProperty(env + ".driver", Constants.JDBC_DRIVER);
		String url = props.getProperty(env + ".url", Constants.JDBC_URL);

		log.log(Level.FINE, "jdbc.env={0}, url={1}, driver={2}", new Object[] { env, url, driver });

		if (driver != null && !driver.isEmpty()) {
			// derby, ojdbc8の場合はJDBC4.0に対応しているのでClass.forName()は必要ない
			// ojdbc14は、JDBC4.0に対応していないためClass.forName()が必要
			Class.forName(driver);
		}

		return DriverManager.getConnection(url, username, password);
	}

}
