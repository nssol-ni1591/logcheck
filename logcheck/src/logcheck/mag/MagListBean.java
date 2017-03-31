package logcheck.mag;

//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

public class MagListBean {

	private final String prjId;
	private final String prjName;
	private final String prjConn;
	private final String prjIp;
	private final String magIp;
	private final String magMask;

	public MagListBean(String prjId, String prjName, String prjConn, String prjIp, String magIp, String magMask) {
		this.prjId = prjId;
		this.prjName = prjName;
		this.prjConn =prjConn;
		this.prjIp = prjIp;
		this.magIp = magIp;
		this.magMask = magMask;
		//System.out.println(this);
	}

	public String getPrjId() {
		return prjId;
	}
	public String getPrjName() {
		return prjName;
	}
	public String getPrjConn() {
		return prjConn;
	}
	public String getPrjIp() {
		return prjIp;
	}
	public String getMagIp() {
		return magIp;
	}
	public String getMagMask() {
		return magMask;
	}

	public String toString() {
		return String.format("prjId=%s, magIp=%s, magMask=%s", prjId, magIp, magMask);
	}
}
