package logcheck.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DB {

	private DB () {
	}

	private static String getHostname() {
		return "172.31.247.137";
	}
	private static String getUsername() {
		return "masterinfo";
	}
	private static String getPassword() {
		return "masterinfo";
	}

	public static Connection createConnection() throws ClassNotFoundException, SQLException, IOException {

		Properties props = new Properties();
		InputStream is = DB.class.getResourceAsStream("/META-INF/jdbc.properties");
		if (is != null) {
			props.load(new InputStreamReader(is));
		}

		String host = props.getProperty("host", getHostname());
		String port = props.getProperty("port", "1521");
		String sid = props.getProperty("sid", "sdcdb011");
		String username = props.getProperty("username", getUsername());
		String password = props.getProperty("password", getPassword());

		host = System.getProperty("jdbc.connect.host", host);

		String url = new StringBuilder("jdbc:oracle:thin:@")
				.append(host)
				.append(":")
				.append(port)
				.append(":")
				.append(sid)
				.toString();

		Class.forName("oracle.jdbc.driver.OracleDriver");
		return DriverManager.getConnection(url, username, password);
	}

}
