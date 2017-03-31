package logcheck.msg;

import java.util.TreeMap;

public class MsgMap extends TreeMap<String, MsgMapIsp> {

	private static final long serialVersionUID = 1L;
	private String msg;

	public MsgMap(String msg) {
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}
	
	public int sum() {
		return values().stream().mapToInt(MsgMapIsp::getSum).sum();
	}
}
