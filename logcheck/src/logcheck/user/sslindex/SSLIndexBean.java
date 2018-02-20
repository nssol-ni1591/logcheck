package logcheck.user.sslindex;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SSLIndexBean {

	private final String flag;
	private final String expire;
	private final String revoce;
	private final String serial;
	private final String filename;
	private final String userId;

	public SSLIndexBean(String flag
			, String expire
			, String revoce
			, String serial
			, String filename
			, String userId
			) {
		this.flag = "V".equals(flag) ? "1" : "0";
		this.expire = expire;
		this.revoce = revoce;
		this.serial = serial;
		this.filename = filename;
		this.userId = userId;
	}

	public String getFlag() {
		return flag;
	}
	public String getExpire() {
		return expire;
	}
	public String getRevoce() {
		return revoce;
	}
	public String getSerial() {
		return serial;
	}
	public String getFilename() {
		return filename;
	}
	public String getUserId() {
		return userId;
	}

	// 正規化表現ではうまく処理できないのでTSV形式ということもありsplitで処理する
	public static SSLIndexBean parse(String s) {
		String[] array = s.split("\t");
		String flag = array[0];
		String expire = array[1];
		String revoce = array[2];
		String serial = array[3];
		String filename = array[4];

		int pos = array[5].indexOf("/CN=");
		String userId = array[5].substring(pos + 4, array[5].length());

		return new SSLIndexBean(flag, expire, revoce, serial, filename, userId);
	}

	public static boolean test(String s) {
		boolean rc = false;
		String[] array = s.split("\t");
		if (array.length == 6) {
			int pos = s.indexOf("/CN=");
			if (pos >= 0) {
				rc = true;
			}
		}
		if (!rc) {
			Logger.getLogger(SSLIndexBean.class.getName()).log(Level.WARNING, "(SSLインデックス): s=\"{0}\"", s.trim());
		}
		return rc;
	}

	@Override
	public String toString() {
		return "user=" + userId + ", flag=" + flag;
	}

}
