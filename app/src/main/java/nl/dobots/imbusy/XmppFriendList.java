package nl.dobots.imbusy;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bart van Vliet on 6-8-15.
 */
public class XmppFriendList extends HashMap<String, XmppFriend> {
	private static final String TAG = XmppFriendList.class.getCanonicalName();

	public void add(XmppFriend friend) {
		put(friend.getJid(), friend);
	}

	@Override
	public XmppFriend put(String key, XmppFriend value) {
		if (key == null) {
			Log.e(TAG, "Key is null");
			return null;
		}
		Log.d(TAG, "Added: " + value.getJid() + " " + value.getUsername() + " " + value.getDomain() + " (" + value.getNick() + ") " + value.getMode());
		return super.put(key, value);
	}

	public void remove(XmppFriend friend) {
		remove(friend.getJid());
	}

	public List<XmppFriend> toList() {
		return new ArrayList<XmppFriend>(values());
	}
}