package nl.dobots.imbusy;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

public class PhoneContactList extends HashMap<String, PhoneContact> {
	private static final String TAG = PhoneContactList.class.getCanonicalName();

	public void add(PhoneContact contact) {
		put(contact.getNumber(), contact);
	}

	@Override
	public PhoneContact put(String key, PhoneContact value) {
		if (key == null) {
			Log.e(TAG, "Key is null");
			return null;
		}
		Log.d(TAG, "Added " + key);
		return super.put(key, value);
	}

	public void remove(PhoneContact contact) {
		remove(contact.getNumber());
	}

	public List<PhoneContact> toList() {
		return new ArrayList<PhoneContact>(values());
	}
}
