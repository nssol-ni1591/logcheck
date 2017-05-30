package logcheck.mag;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class MagListBean {

	private final String projId;
	private final String projName;
	private final String siteName;
//	private final String projIp;
	private final String magIp;
	
	@Inject private Logger log;

	public MagListBean(String projId, String projName, String siteName, String magIp) {
		this.projId = projId;
		this.projName = projName;
		this.siteName = siteName;
//		this.projIp = null;
		this.magIp = magIp;
	}
//	public MagListBean(String projId, String projName, String projSite, String projIp, String magIp, String magMask) {
	public MagListBean(String projId, String projName, String siteName, String magIp, String magMask) {
		this.projId = projId;
		this.projName = projName;
		this.siteName = siteName;
//		this.projIp = projIp;
		if (magIp.contains("/")) {
			throw new IllegalArgumentException("magIp contains \"/\"");
		}
		this.magIp = magIp + "/" + magMask;
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
	/*
	public String getProjIp() {
		return projIp;
	}
	*/
	public String getMagIp() {
		return magIp;
	}

	public String toString() {
		return String.format("projId=%s, magIp=%s", projId, magIp);
	}
}
