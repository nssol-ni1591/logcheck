package logcheck.msg;

import logcheck.log.AccessLogBean;
import logcheck.util.NetAddr;

public class MsgBean implements Comparable<MsgBean> {

	private final NetAddr addr;
	private final String id;
	private final String pattern;

	private String firstDate;
	private String lastDate;
	private String roles;
	private int count;

	public MsgBean(AccessLogBean log, String pattern) {
		this.addr = log.getAddr();
		this.id = log.getId();
		this.pattern = pattern;
		this.roles = log.getRoles();

		this.firstDate = log.getDate();
		this.lastDate = firstDate;
		this.count = 1;
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
	public String getId() {
		return id;
	}
	public String getPattern() {
		return pattern;
	}
	public String getRoles() {
		return roles;
	}
	public int getCount() {
		return count;
	}

	public synchronized void update(AccessLogBean b) {
		String date = b.getDate();
		if (firstDate.compareTo(date) > 0) {
			this.firstDate = date;
		}
		if (lastDate.compareTo(date) < 0) {
			this.lastDate = date;
		}
		if ("".equals(roles)) {
			roles = b.getRoles();
		}
		this.count += 1;
	}

	@Override
	public int compareTo(MsgBean o) {
		// TODO Auto-generated method stub
		return pattern.compareTo(o.getPattern());
	}
	
	public String toString() {
		return String.format("[first=%s, last=%s, addr=%s, id=%s, count=%d]", firstDate, lastDate, addr.toString(), id, count);
	}
}
