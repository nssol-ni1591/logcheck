package logcheck.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class Env {

	public static String FWLOG;
	public static String VPNLOG;

	public static String KNOWNLIST;
	public static String MAGLIST;
	public static String SDCLIST;
	public static String SSLINDEX;

	static {
		String env = System.getProperty("env", "dev");
		Properties props = new Properties();
		InputStream is = FwLogTest.class.getResourceAsStream("/resources/" + env + ".properties");
		
		try {
			if (is != null) {
				props.load(new InputStreamReader(is));
			}

			FWLOG = props.getProperty("log.fw");
			VPNLOG = props.getProperty("log.vpn");
			KNOWNLIST = props.getProperty("xls.knownlist");
			MAGLIST = props.getProperty("xls.maglist");
			SDCLIST = props.getProperty("xls.sdclist");
			SSLINDEX = props.getProperty("xls.sslindex");
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
