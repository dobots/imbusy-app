package nl.dobots.imbusy;

import org.jivesoftware.smack.packet.Presence;

/**
 * Created by Bart van Vliet on 6-8-15.
 */
public class XmppFriend {
	private String _username;
	private String _domain;
	private String _jid;
	private String _nick;
	private Presence.Mode _mode;

	public XmppFriend(String jid, String nick, Presence.Mode mode) {
		setJid(jid);
		_nick = nick;
		_mode = mode;
	}

	public XmppFriend(String username, String domain, String nick, Presence.Mode mode) {
		this(username + "@" + domain, nick, mode);
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
}
