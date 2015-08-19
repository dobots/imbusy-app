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