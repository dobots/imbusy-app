package nl.dobots.imbusy;

/**
 * Created by Bart van Vliet on 6-8-15.
 */
public class PhoneContact {
	private String _number;
	private String _name;
	private Status _status;

	public PhoneContact(String number, String name, Status status) {
		_number = number;
		_name = name;
		_status = status;
	}

	public PhoneContact(String number, String name) {
		this(number, name, Status.AVAILABLE);
	}

	public String getNumber() {
		return _number;
	}

	public String getName() {
		return _name;
	}

	public Status getStatus() {
		return _status;
	}

	public void setStatus(Status status) {
		_status = status;
	}
}

