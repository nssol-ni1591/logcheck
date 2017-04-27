package logcheck.sdc;

public class SdcListBean {

	private final String name;
	private final String addr;
	private final String type;
	
	public SdcListBean(String name, String addr, String type) {
		this.name = name;
		this.addr = addr;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}

	public String getAddr() {
		return addr;
	}

	public String getType() {
		return type;
	}

}
