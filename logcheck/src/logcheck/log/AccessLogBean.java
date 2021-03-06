package logcheck.log;

import java.util.ArrayList;
import java.util.Arrays;

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
		if (roles == null || roles.isEmpty()) {
			return new String[] { "" };
		}
		ArrayList<String> list = new ArrayList<>();
		Arrays.stream(roles.split(","))
			//.map(role -> role.trim())
			.map(String::trim)
			.filter(role -> !role.isEmpty())	// "[, NSSDC Common Role]"みたいなログ対応
			//.forEach(role -> list.add(role))
			.forEach(list::add)
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
