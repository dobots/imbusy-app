package nl.dobots.imbusy;

import android.util.Log;

/**
 * Created by Bart van Vliet on 27-7-15.
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
		Log.d(TAG, "Set threshold of " + this._address + " to " + _threshold);
		_threshold = threshold;
	}
}
