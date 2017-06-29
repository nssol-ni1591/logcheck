package logcheck.util;

public class SummaryImpl<E, F> implements Summary<F> {

	private final E ref;

	private String firstDate;
	private String lastDate;
	private int count;

	public SummaryImpl(E ref) {
		this.ref = ref;
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

	public void update(String date) {
		// TODO Auto-generated method stub
		lastDate = date;
		if ("".equals(firstDate)) {
			firstDate = date;
		}
		count += 1;
	}

	@Override
	public void update(F data) {
		// TODO Auto-generated method stub
		
	}
	
}
