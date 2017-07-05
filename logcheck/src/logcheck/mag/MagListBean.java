package logcheck.mag;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class MagListBean {

	private final String projId;
	private final String projName;
	private final String siteName;
	private final String magIp;
	// 追加：未利用ユーザ検索
	private final String projDelFlag;
	private final String siteDelFlag;

	@Inject private Logger log;

	// for DB
	public MagListBean(String projId, String projName, String siteName, String magIp, String projDelFlag, String siteDelFlag) {
		this.projId = projId;
		this.projName = projName;
		this.siteName = siteName;
		this.magIp = magIp;
		this.projDelFlag = projDelFlag;
		this.siteDelFlag = siteDelFlag;
	}
	// for Tsv
	public MagListBean(String projId, String projName, String siteName, String magIp, String magMask) {
		this.projId = projId;
		this.projName = projName;
		this.siteName = siteName;
		if (magIp.contains("/")) {
			throw new IllegalArgumentException("magIp contains \"/\"");
		}
		this.magIp = magIp + "/" + magMask;
		this.projDelFlag = "-1";
		this.siteDelFlag = "-1";
	}

	@PostConstruct
	public void init() {
		log.fine(this.toString());
	}

	public String getProjId() {
		return projId;
	}
	public String getProjName() {
		return projName;
	}
	public String getSiteName() {
		return siteName;
	}
	public String getMagIp() {
		return magIp;
	}
	public String getProjDelFlag() {
		return projDelFlag;
	}
	public String getSiteDelFlag() {
		return siteDelFlag;
	}

	public String toString() {
		return String.format("projId=%s, magIp=%s", projId, magIp);
	}

}
