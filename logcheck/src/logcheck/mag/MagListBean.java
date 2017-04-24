package logcheck.mag;

//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

public class MagListBean {

	private final String prjId;
	private final String prjName;
	private final String prjSite;
	private final String prjIp;
	private final String magIp;
//	private final String magMask;

	public MagListBean(String prjId, String prjName, String prjSite, String magIp) {
		this.prjId = prjId;
		this.prjName = prjName;
		this.prjSite = prjSite;
		this.prjIp = null;
		this.magIp = magIp;
		//System.out.println(this);
	}
	public MagListBean(String prjId, String prjName, String prjSite, String prjIp, String magIp, String magMask) {
		this.prjId = prjId;
		this.prjName = prjName;
		this.prjSite = prjSite;
		this.prjIp = prjIp;
		if (magIp.contains("/")) {
			throw new IllegalArgumentException("magIp contains \"/\"");
		}
		this.magIp = magIp + "/" + magMask;
		//System.out.println(this);
	}

	public String getPrjId() {
		return prjId;
	}
	public String getPrjName() {
		return prjName;
	}
	public String getPrjSite() {
		return prjSite;
	}
	public String getPrjIp() {
		return prjIp;
	}
	public String getMagIp() {
		return magIp;
	}
//	public String getMagMask() {
//		return magMask;
//	}

	public String toString() {
		return String.format("prjId=%s, magIp=%s", prjId, magIp);
	}
}
