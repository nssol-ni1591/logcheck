package logcheck.site.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import logcheck.annotations.WithElaps;
import logcheck.site.SiteList;
import logcheck.site.SiteListIsp;
import logcheck.site.SiteListIspImpl;
import logcheck.util.NetAddr;

/*
 * 以前の TsvMagListクラス
 */
@Alternative
public class TsvSiteList extends HashMap<String, SiteListIsp> implements SiteList {

	@Inject private Logger log;

	private static final long serialVersionUID = 1L;

	public static final String PATTERN = "(PRJ_[\\w_]+)\t(.+)\t(.+)\t([\\d\\.～\\/]+)\t([\\d+\\.\\d+\\.\\d+\\.\\d+]+)\t([\\d+\\.\\d+\\.\\d+\\.\\d+]+)";
	public static final String LOG_HEADER1 = "(インターネット経由接続先): s=\"{0}\"";

	public TsvSiteList() {
		super(200);
	}

	// for envoronment not using weld-se
	public void init() {
		if (log == null) {
			// JUnitの場合、logのインスタンスが生成できないため
			log = Logger.getLogger(this.getClass().getName());
		}
	}

	@WithElaps
	public SiteList load(String file) throws IOException {
		try (Stream<String> input = Files.lines(Paths.get(file), Charset.forName("MS932"))) {
			input//.filter(this::test)
				.map(this::parse)
				.filter(Objects::nonNull)
				.forEach(b -> {
					SiteListIsp site = this.get(b.getProjId());
					if (site == null) {
						site = new SiteListIspImpl(b.getSiteName(), b.getProjId());
						this.put(b.getProjId(), site);
					}
					site.addAddress(new NetAddr(b.getMagIp()));
				});
		}
		return this;
	}

	private TsvSiteListBean parse(String s) {
		if (s.startsWith("#")) {
			return null;
		}
		if (s.startsWith("\t\t")) {
			return null;
		}

		Pattern p = Pattern.compile(PATTERN);
		Matcher m = p.matcher(s);
		if (!m.find()) {
			log.log(Level.WARNING, LOG_HEADER1, s.trim());
			return null;
		}

		String[] array = s.split("\t");
		if (array.length > 5 && "非固定".equals(array[5])) {
			log.log(Level.WARNING, LOG_HEADER1, s.trim());
			return null;
		}
		if (array.length > 6 && "非固定".equals(array[6])) {
			log.log(Level.WARNING, LOG_HEADER1, s.trim());
			return null;
		}

		String projId = null;
		String projName = null;
		String siteName = null;
		String magIp = null;
		String magMask = null;

		if (array.length > 1) {
			projId = array[1];
		}
		if (array.length > 2) {
			projName = array[2];
		}
		if (array.length > 3) {
			siteName = array[3];
		}
		// 4番目は使用しない
		if (array.length > 5) {
			magIp = array[5];
		}
		if (array.length > 6) {
			magMask = array[6];
		}
		return new TsvSiteListBean(projId, projName, siteName, magIp, magMask);
	}

	// equals()を実装するとhashCode()の実装も要求され、それはBugにランク付けられるのでequals()の実装をやめたい
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}
}
