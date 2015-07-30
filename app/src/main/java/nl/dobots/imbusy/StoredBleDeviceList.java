package nl.dobots.imbusy;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Bart van Vliet on 27-7-15.
 */
public class StoredBleDeviceList extends HashMap<String, StoredBleDevice> {
	private String TAG = StoredBleDeviceList.class.getCanonicalName();


	public void add(StoredBleDevice device) {
		put(device.getAddress(), device);
	}

	@Override
	public StoredBleDevice put(String key, StoredBleDevice value) {
		Log.d(TAG, "Put " + key);
		return super.put(key, value);
	}

	public void remove(StoredBleDevice device) {
		remove(device.getAddress());
	}

	@Override
	public StoredBleDevice remove(Object key) {
		Log.d(TAG, "Remove " + key);
		return super.remove(key);
	}

	public boolean isClose(String address, float rssi) {
		if (containsKey(address) && rssi > get(address).getRssiThreshold()) {
			return true;
		}
		return false;
	}

	public boolean contains(StoredBleDevice device) {
		return containsKey(device.getAddress());
	}

	public boolean contains(String address) {
		return containsKey(address);
	}

	public List<StoredBleDevice> toList() {
		return new ArrayList<StoredBleDevice>(values());
	}

	public void Save(SharedPreferences preferences) {
		//preferences.
	}

	public void Load(SharedPreferences.Editor editor) {
		Iterator it = this.entrySet().iterator();
//		while (it.hasNext()) {
//			editor.putString()
//		}
	}
}
