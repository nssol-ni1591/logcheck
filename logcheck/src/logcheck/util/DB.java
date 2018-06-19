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

public class DB {

	private static final String MASTERINFO_USER = "masterinfo";
	private static final String MASTERINFO_PASS = "masterinfo";
	private static final String DEFAULT_DRIVER = "oracle.jdbc.driver.OracleDriver";
	private static final String DEFAULT_URL = "jdbc:oracle:thin:@172.30.90.145:1521:sdcdb011";

	private DB () {
	}

	private static String getUsername() {
		return MASTERINFO_USER;
	}
	private static String getPassword() {
		return MASTERINFO_PASS;
	}
	private static String getDriver() {
		return DEFAULT_DRIVER;
	}
	private static String getUrl() {
		return DEFAULT_URL;
	}

	public static Connection createConnection() throws ClassNotFoundException, SQLException, IOException {
		final Logger log = Logger.getLogger(DB.class.getName());
		Properties props = new Properties();
		InputStream is = DB.class.getResourceAsStream("/META-INF/jdbc.properties");
		if (is != null) {
			props.load(new InputStreamReader(is));
		}

		String username = props.getProperty("username", getUsername());
		String password = props.getProperty("password", getPassword());

		String env = System.getProperty("jdbc.env", "dev");
		String driver = props.getProperty(env + ".driver", getDriver());
		String url = props.getProperty(env + ".url", getUrl());
		log.log(Level.INFO, "jdbc.env={0}, url={1}, driver={2}", new Object[] { env, url, driver });

		if (driver != null && !driver.isEmpty()) {
			Class.forName(driver);
		}

		return DriverManager.getConnection(url, username, password);
	}

}
