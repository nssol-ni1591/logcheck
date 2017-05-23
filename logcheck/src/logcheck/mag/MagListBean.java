package logcheck.mag;

public class MagListBean {

	private final String prjId;
	private final String prjName;
	private final String prjSite;
	private final String prjIp;
	private final String magIp;

	public MagListBean(String prjId, String prjName, String prjSite, String magIp) {
		this.prjId = prjId;
		this.prjName = prjName;
		this.prjSite = prjSite;
		this.prjIp = null;
		this.magIp = magIp;
		// コンストラクタで＠Injectを参照することができない
		//log.fine(this.toString());
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
		// コンストラクタで＠Injectを参照することができない
		//log.fine(this.toString());
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

	public String toString() {
		return String.format("prjId=%s, magIp=%s", prjId, magIp);
	}
}
