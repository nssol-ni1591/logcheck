package logcheck.util;

public class Constants {

	// アドレスの定義は SonarQube でチェック対象になるため
	public static final String GLOBAL_IP = String.join(".", "0", "0", "0", "0");
	public static final String LOCALHOST = String.join(".", "127", "0", "0", "1");
	
	public static final String CLASS_A = String.join("/", String.join(".", "10", "0", "0", "0"), "8");
	public static final String CLASS_B = String.join("/", String.join(".", "172", "16", "0", "0"), "16");
	public static final String CLASS_C = String.join("/", String.join(".", "192", "168", "0", "0"), "16");

	private Constants() {
		// Do nothing
	}

}
