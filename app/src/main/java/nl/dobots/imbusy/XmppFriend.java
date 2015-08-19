package nl.dobots.imbusy;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.packet.RosterPacket;

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

public class XmppFriend {
	private String _username;
	private String _domain;
	private String _jid;
	private String _nick;
	private Presence.Mode _mode;
	private RosterPacket.ItemType _subscriptionType;

	public XmppFriend(String jid, String nick, Presence.Mode mode, RosterPacket.ItemType subscriptionType) {
		setJid(jid);
		_nick = nick;
		_mode = mode;
		_subscriptionType = subscriptionType;
	}

	public XmppFriend(String username, String domain, String nick, Presence.Mode mode, RosterPacket.ItemType subscriptionType) {
		this(username + "@" + domain, nick, mode, subscriptionType);
	}

	public String getUsername() {
		return _username;
	}

	public void setUsername(String username) {
		_username = username;
		_jid = _username + "@" + _domain;
	}

	public String getDomain() {
		return _domain;
	}

	public void setDomain(String domain) {
		_domain = domain;
		_jid = _username + "@" + _domain;
	}

	public String getJid() {
		return _jid;
	}

	public void setJid(String jid) {
		_jid = jid;
		String[] split = jid.split("@");
		_username = split[0];
		_domain = split[1];
	}

	public String getNick() {
		return _nick;
	}

	public void setNick(String nick) {
		_nick = nick;
	}

	public Presence.Mode getMode() {
		return _mode;
	}

	public void setMode(Presence.Mode mode) {
		_mode = mode;
	}

	public RosterPacket.ItemType getSubscriptionType() {
		return _subscriptionType;
	}

	public void setSubscriptionType(RosterPacket.ItemType subscriptionType) {
		_subscriptionType = subscriptionType;
	}
}
