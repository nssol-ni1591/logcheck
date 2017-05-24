package logcheck.sdc;

import java.util.logging.Logger;

import javax.inject.Inject;

import logcheck.isp.IspList;

public class SdcListIsp extends IspList {

	@Inject private Logger log;

	public SdcListIsp(String name, String type) {
		super(name, type);
		log.fine(this.toString());
	}

	public String toString() {
		return String.format("name=%s, addr=%s", getName(), getAddress());
	}

}