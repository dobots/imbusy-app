package nl.dobots.imbusy;

/**
 * Created by Bart van Vliet on 6-8-15.
 */
public enum Status {
	AVAILABLE(0),
	BUSY(1);

	private int _num;
	private Status(int num) {
		_num = num;
	}
	public int getNum() {
		return _num;
	}
	public static Status fromNum(int i) {
		for (Status status : Status.values()) {
			if (status.getNum() == i) {
				return status;
			}
		}
		return null;
	}
}