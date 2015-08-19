package nl.dobots.imbusy;

/**
 * Copyright (c) 2015 Bart van Vliet <bart@dobots.nl>. All rights reserved.
 * <p/>
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3, as
 * published by the Free Software Foundation.
 * <p/>
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 * <p/>
 * Created on 6-8-15
 *
 * @author Bart van Vliet
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

