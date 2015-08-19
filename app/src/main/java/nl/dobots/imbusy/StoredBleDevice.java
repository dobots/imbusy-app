package nl.dobots.imbusy;

import android.util.Log;

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
 * Created on 27-7-15
 *
 * @author Bart van Vliet
 */

public class StoredBleDevice {
	private static final String TAG = StoredBleDevice.class.getCanonicalName();
	private String _address;
	private String _name;
	private float _threshold;


	public StoredBleDevice(String address, String name, float rssiThreshold) {
		_address = address;
		_name = name;
		_threshold = rssiThreshold;
	}
	public StoredBleDevice(String address, String name) {
		this(address, name, -70); //TODO: magic number
	}
	public StoredBleDevice(String address) {
		this(address, "");
	}

	public String getAddress() {
		return _address;
	}

	public String getName() {
		return _name;
	}

	public float getRssiThreshold() {
		return _threshold;
	}

	public void setName(String name) {
		_name = name;
	}

	public void setRssiThreshold(float threshold) {
		Log.d(TAG, "Set threshold of " + this._address + " to " + threshold);
		_threshold = threshold;
	}
}
