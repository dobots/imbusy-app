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