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

//	private static final String DEFAULT_HOST = "172.31.247.137";
	private static final String MASTERINFO_USER = "masterinfo";
	private static final String MASTERINFO_PASS = "masterinfo";
	private static final String DEFAULT_DRIVER = "oracle.jdbc.driver.OracleDriver";
	private static final String DEFAULT_URL = "jdbc:oracle:thin:@172.30.90.145:1521:sdcdb011";

	private DB () {
	}

//	private static String getHostname() {
//		return DEFAULT_HOST;
//	}
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

//		String host = props.getProperty("host", getHostname());
//		String port = props.getProperty("port", "1521");
//		String sid = props.getProperty("sid", "sdcdb011");
		String username = props.getProperty("username", getUsername());
		String password = props.getProperty("password", getPassword());

//		host = System.getProperty("jdbc.connect.host", host);

/*
		String url = new StringBuilder("jdbc:oracle:thin:@")
				.append(host)
				.append(":")
				.append(port)
				.append(":")
				.append(sid)
				.toString();

		Class.forName("oracle.jdbc.driver.OracleDriver");
		return DriverManager.getConnection(url, username, password);
*/
		/*
		String url = new StringBuilder("jdbc:derby:c:/opt/java-bin/db-derby-10.13.1.1-bin/db/masterinfo")
				.append(";user=")
				.append(username)
				.append(";password=")
				.append(password)
				.toString();

		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		return DriverManager.getConnection(url);
*/

		String env = System.getProperty("jdbc.env", "dev");

		String driver = props.getProperty(env + ".driver", getDriver());
		if (driver != null) {
			Class.forName(driver);
		}
//		driver = System.getProperty("jdbc.connect.driver", driver);
//		url = System.getProperty("jdbc.connect.url", url);
//		Class.forName(driver);

		String url = props.getProperty(env + ".url", getUrl());
		log.log(Level.INFO, "jdbc.env={0}, url={1}, driver={2}", new Object[] { env, url, driver });
		return DriverManager.getConnection(url, username, password);
	}

}
