package logcheck.log;

import logcheck.util.NetAddr;

public class AccessLogBean {

	private final String date;
	private final String host;
	private final String ip;
	private final String id;
	private final String role;
	private final String msg;

	public AccessLogBean(String date, String host, String ip, String id, String role, String msg) {
		this.date = date;
		this.host = host;
		this.ip = ip;
		this.id = id;
		this.role = role;
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
//		return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		return date;
	}
	public String getHost() {
		return host;
	}
	/*
	public String getIp() {
		return ip;
	}
	*/
	public NetAddr getAddr() {
		return new NetAddr(ip);
	}
	public String getId() {
		return id;
	}
	public String getRole() {
		return role;
	}
	public String getMsg() {
		return msg;
	}

	public String toString() {
		return String.format("date=%s, msg=%s", date, msg);
	}
}
