package logcheck.log;

import logcheck.util.ClientAddr;
import logcheck.util.NetAddr;

public class AccessLogBean {

	private final String date;
	private final String host;
	private final String ip;
	private final String id;
	private final String roles;
	private final String msg;

	public AccessLogBean(String date, String host, String ip, String id, String roles, String msg) {
		this.date = date;
		this.host = host;
		this.ip = ip;
		this.id = id;
		this.roles = roles;
		this.msg = msg;

		if (ip == null) {
			throw new IllegalArgumentException("ip is null [date=" + date + "]");
		}
		if (msg == null) {
			throw new IllegalArgumentException("msg is null [date=" + date + ", ip=" + ip + "]");
		}
		//System.err.print(this);
	}

	public String getDate() {
		return date;
	}
	public String getHost() {
		return host;
	}
	public NetAddr getAddr() {
		return new ClientAddr(ip);
	}
	public String getId() {
		return id;
	}
	public String getRoles() {
		return roles;
	}
	public String getMsg() {
		return msg;
	}

	public String toString() {
		return String.format("date=%s, roles=%s, msg=%s", date, roles, msg);
	}
}
