package logcheck.log;

import java.util.ArrayList;
import java.util.Arrays;

import logcheck.util.net.ClientAddr;
import logcheck.util.net.NetAddr;

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
		/* 2018/03/08 AccessLog.javaに移した
		if (id.startsWith("z")) {
			this.id = "Z" + id.substring(1);
		}
		else {
			this.id = id;
		}
		*/
		this.id = id;
		this.roles = roles;
		this.msg = msg;

		if (ip == null) {
			throw new IllegalArgumentException("ip is null [date=" + date + "]");
		}
		if (msg == null) {
			throw new IllegalArgumentException("msg is null [date=" + date + ", ip=" + ip + "]");
		}
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
	public String[] getRoles() {
		if (roles == null || "".equals(roles)) {
			return new String[] {
				// nothing to do
			};
		}
		/*
		String[] array = roles.split(",");
		for (int ix = 0; ix < array.length; ix++) {
			array[ix] = array[ix].trim();
		}
		return array;
		*/
		// "[, NSSDC Common Role]"みたいなログ対応
		ArrayList<String> list = new ArrayList<>();
		Arrays.stream(roles.split(","))
			.map(role -> role.trim())
			.filter(role -> !role.isEmpty())
			.forEach(role -> list.add(role))
			;
		return list.toArray(new String[list.size()]);
	}
	public String getMsg() {
		return msg;
	}

	public String toString() {
		return String.format("date=%s, roles=%s, msg=%s", date, roles, msg);
	}

}
