package logcheck.msg;

import logcheck.log.AccessLogBean;
import logcheck.util.NetAddr;

public class MsgBean implements Comparable<MsgBean> {

	private final String firstDate;
	private final NetAddr addr;
	private final String pattern;

	private String lastDate;

	public MsgBean(AccessLogBean log, String pattern) {
		this.firstDate = log.getDate();
		this.addr = log.getAddr();
		this.pattern = pattern;

		this.lastDate = firstDate;
	}

	public String getFirstDate() {
		return firstDate;
	}
	public String getLastDate() {
		return lastDate;
	}
	public NetAddr getAddr() {
		return addr;
	}
	public String getPattern() {
		return pattern;
	}

	public void updateDate(String date) {
		this.lastDate = date;
	}
	
	@Override
	public int compareTo(MsgBean o) {
		// TODO Auto-generated method stub
		return pattern.compareTo(o.getPattern());
	}
}
