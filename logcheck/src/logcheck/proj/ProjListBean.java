package logcheck.proj;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import logcheck.log.AccessLogBean;
import logcheck.log.AccessLogSummary;

public class ProjListBean implements Comparable<ProjListBean> {

	private final String projId;
	private String validFlag;

	private final Map<String, AccessLogSummary> logs;

	public ProjListBean(String projId, String validFlag) {
		this.projId = projId;
		this.validFlag = validFlag;
		this.logs = new ConcurrentHashMap<>();
	}

	public String getProjId() {
		return projId;
	}
	public String getValidFlag() {
		return validFlag;
	}
	public Map<String, AccessLogSummary> getLogs() {
		return logs;
	}

	public void update(AccessLogBean b, String pattern) {
		AccessLogSummary summary = logs.get(b.getId());
		if (summary == null) {
			logs.put(b.getId(), new AccessLogSummary(b, pattern));
		}
		else {
			summary.update(b);
		}
	}

	@Override
	public String toString() {
		return String.format("proj=%s", projId);
	}

	@Override
	public int compareTo(ProjListBean o) {
		Objects.requireNonNull(o);

		return projId.compareTo(o.getProjId());
	}
	// equals()を実装するとhashCode()の実装も要求され、それはBugにランク付けられるのでequals()の実装をやめたい
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		Objects.requireNonNull(obj);

		if (obj instanceof ProjListBean) {
			return this.compareTo((ProjListBean)obj) == 0;
		}
		return false;
	}

}
