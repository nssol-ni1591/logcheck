package logcheck.known.net.apnic;



public class SearchTextString implements SearchText {

	private String value;

	public SearchTextString(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return value;
	}
	
	public boolean equals(SearchTextString target) {
		return value.equals(target.toString());
	}

}
