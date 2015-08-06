package nl.dobots.imbusy;

import org.jivesoftware.smack.packet.Presence;

/**
 * Created by Bart van Vliet on 6-8-15.
 */
public class XmppFriend {
	private String _number;
	private String _host;
	private String _jid;
	private String _nick;
	private Presence.Mode _mode;

	public XmppFriend(String jid, String nick, Presence.Mode mode) {
		setJid(jid);
		_nick = nick;
		_mode = mode;
	}

	public XmppFriend(String number, String host, String nick, Presence.Mode mode) {
		this(number + "@" + host, nick, mode);
	}

	public String getNumber() {
		return _number;
	}

	public void setNumber(String number) {
		_number = number;
		_jid = _number + "@" + _host;
	}

	public String getHost() {
		return _host;
	}

	public void setHost(String host) {
		_host = host;
		_jid = _number + "@" + _host;
	}

	public String getJid() {
		return _jid;
	}

	public void setJid(String jid) {
		_jid = jid;
		String[] split = jid.split("@");
		_number = split[0];
		_host = split[1];
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
