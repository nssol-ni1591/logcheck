package logcheck.util;

public class Summary<E> {

	private final E ref;

	private String firstDate;
	private String lastDate;
	private int count;

	public Summary(E ref) {
		this.ref = ref;
		this.firstDate = "";
		this.lastDate = "";
		this.count = 0;
	}
	public Summary(E ref, String date) {
		this.ref = ref;
		this.firstDate = date;
		this.lastDate = date;
		this.count = 0;
	}

	public E getRef() {
		return ref;
	}
	public String getFirstDate() {
		return firstDate;
	}
	public String getLastDate() {
		return lastDate;
	}
	public int getCount() {
		return count;
	}
	public void addCount() {
		count += 1;
	}

	public synchronized void update(String date) {
		if ("".equals(firstDate)) {
			firstDate = date;
		}
		else if (firstDate.compareTo(date) > 0) {
			this.firstDate = date;
		}
		if ("".equals(lastDate)) {
			lastDate = date;
		}
		else if (lastDate.compareTo(date) < 0) {
			this.lastDate = date;
		}
		count += 1;
	}
	
}
