package nl.dobots.imbusy;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bart van Vliet on 6-8-15.
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
