package logcheck.user.openssl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class SslIndexBean {

	private final String flag;
	private final LocalDateTime expire;
	private final LocalDateTime revoce;
	private final String searial;
	private final String filename;
	private final String cn;
	
	@Inject private Logger log;

	private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyMMddHHmmssZ");

	public SslIndexBean(String flag
			, String expire
			, String revoce
			, String searial
			, String filename
			, String cn
			) {
		this.flag = flag;
		this.expire = LocalDateTime.parse(expire, format);
		this.revoce = LocalDateTime.parse(revoce, format);
		this.searial = searial;
		this.filename = filename;
		this.cn = cn;
	}

	@PostConstruct
	public void init() {
		log.fine(this.toString());
	}

	public String getFlag() {
		return flag;
	}

	public LocalDateTime getExpire() {
		return expire;
	}

	public LocalDateTime getRevoce() {
		return revoce;
	}

	public String getSearial() {
		return searial;
	}

	public String getFilename() {
		return filename;
	}

	public String getCn() {
		return cn;
	}

}
