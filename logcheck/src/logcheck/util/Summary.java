package logcheck.util;

public interface Summary<E> {

	default int getCount() {
		return 0;
	}

	void update(E data);

}
