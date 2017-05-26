package logcheck.mag;

public class MagListBean {

	private final String projId;
	private final String projName;
	private final String projSite;
	private final String projIp;
	private final String magIp;

	public MagListBean(String projId, String projName, String projSite, String magIp) {
		this.projId = projId;
		this.projName = projName;
		this.projSite = projSite;
		this.projIp = null;
		this.magIp = magIp;
		// コンストラクタで＠Injectを参照することができない
		//log.fine(this.toString());
	}
	public MagListBean(String projId, String projName, String projSite, String projIp, String magIp, String magMask) {
		this.projId = projId;
		this.projName = projName;
		this.projSite = projSite;
		this.projIp = projIp;
		if (magIp.contains("/")) {
			throw new IllegalArgumentException("magIp contains \"/\"");
		}
		this.magIp = magIp + "/" + magMask;
		// コンストラクタで＠Injectを参照することができない
		//log.fine(this.toString());
	}

	public String getProjId() {
		return projId;
	}
	public String getProjName() {
		return projName;
	}
	public String getProjSite() {
		return projSite;
	}
	public String getProjIp() {
		return projIp;
	}
	public String getMagIp() {
		return magIp;
	}

	public String toString() {
		return String.format("projId=%s, magIp=%s", projId, magIp);
	}
}
