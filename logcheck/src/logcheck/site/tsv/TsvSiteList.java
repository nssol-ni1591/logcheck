package logcheck.site.tsv;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.enterprise.inject.Alternative;

import logcheck.annotations.WithElaps;
import logcheck.site.SiteList;
import logcheck.site.SiteListIsp;
import logcheck.site.SiteListIspImpl;

/*
 * 以前の TsvMagListクラス
 */
@Alternative
public class TsvSiteList extends HashMap<String, SiteListIsp> implements SiteList {

	//@Inject private Logger log;
	private static final Logger log = Logger.getLogger(TsvSiteList.class.getName());

	private static final long serialVersionUID = 1L;

	public static final String PATTERN = "(PRJ_[\\w_]+)\t(.+)\t(.+)\t([\\d\\.～\\/]+)\t([\\d+\\.\\d+\\.\\d+\\.\\d+]+)\t([\\d+\\.\\d+\\.\\d+\\.\\d+]+)";

	public TsvSiteList() {
		super(200);
	}

	@WithElaps
	public SiteList load(String file) throws IOException {
		try (Stream<String> input = Files.lines(Paths.get(file), Charset.forName("MS932"))) {
			input.filter(s -> test(s))
				.map(s -> parse(s))
				.forEach(b -> {
					SiteListIsp site = this.get(b.getProjId());
					if (site == null) {
						site = new SiteListIspImpl(b.getSiteName(), b.getProjId());
						this.put(b.getProjId(), site);
					}
					site.addAddress(b.getMagIp());
				});
		}
		return this;
	}

	private TsvSiteListBean parse(String s) {
		String projId = null;
		String projName = null;
		String siteName = null;
		String magIp = null;
		String magMask = null;

		String[] array = s.split("\t");
		if (array.length > 1) {
			projId = array[1];
		}
		if (array.length > 2) {
			projName = array[2];
		}
		if (array.length > 3) {
			siteName = array[3];
		}
//		if (array.length > 4) {
//			projIp = array[4];
//		}
		if (array.length > 5) {
			magIp = array[5];
		}
		if (array.length > 6) {
			magMask = array[6];
		}
		return new TsvSiteListBean(projId, projName, siteName, magIp, magMask);
	}
	private boolean test(String s) {
		if (s.startsWith("#")) {
			return false;
		}
		if (s.startsWith("\t\t")) {
			return false;
		}

		Pattern p = Pattern.compile(PATTERN);
		Matcher m = p.matcher(s);
		boolean rc = m.find();
		if (!rc) {
			log.warning("(インターネット経由接続先): s=\"" + s.trim() + "\"");
		}

		String[] array = s.split("\t");
		if (array.length > 5 && "非固定".equals(array[5])) {
			rc = false;
		}
		if (array.length > 6 && "非固定".equals(array[6])) {
			rc = false;
		}
		return rc;
	}

}
