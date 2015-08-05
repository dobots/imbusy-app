package nl.dobots.imbusy;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.util.Collection;

/**
 * Created by Bart van Vliet on 5-8-15.
 */
public class XmppThread {
	private static final String TAG = XmppThread.class.getCanonicalName();

	public static final int MSG_REGISTER = 1;

	public static final int MSG_CONNECT = 10;
	public static final int MSG_DISCONNECT = 11;
	public static final int MSG_CONNECT_FAIL = 12;
	public static final int MSG_CONNECTED = 13;
	public static final int MSG_DISCONNECTED = 14;

	public static final int MSG_SET_STATUS = 20;
	public static final int MSG_STATUS = 21;

	public static final int MSG_GET_FRIENDS = 30;
	public static final int MSG_FRIENDS_LIST = 31;
	public static final int MSG_ADD_FRIEND = 32;
	public static final int MSG_FRIEND_ADDED = 33;
	public static final int MSG_REM_FRIEND = 34;
	public static final int MSG_FRIEND_REMOVED = 35;


	HandlerThread _xmppThread;

	private XMPPTCPConnectionConfiguration _config;
	private AbstractXMPPConnection _connection;
	private Presence _presence;
	private Roster _roster;

	/** Target we give to send messages to, handled by MessageHandler */
	private Messenger _messengerIn;
	private MessageHandler _messageHandler;
	private Messenger _messengerOut;

	/** Handler of incoming messages */
	private class MessageHandler extends Handler {

		public MessageHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_CONNECT: {
					connect();
					break;
				}
				case MSG_DISCONNECT: {
					disconnect();
					break;
				}
				case MSG_SET_STATUS: {
					String statusMsg = msg.getData().getString("status");
					String statusMode = msg.getData().getString("mode");
					setPresence(statusMsg, statusMode);
					break;
				}
				case MSG_GET_FRIENDS: {
					listRoster();
					break;
				}
				default: {
					super.handleMessage(msg);
				}
			}

		}
	}

	private void sendMessage(android.os.Message msg) {
		if (_messengerOut == null || msg == null) {
			Log.e(TAG, "sendMessage() - messenger or msg is null");
			return;
		}
		try {
			_messengerOut.send(msg);
		} catch (RemoteException e) {
			Log.e(TAG, "sendMessage() exception:");
			e.printStackTrace();
		}
	}

	private void sendMessage(int msgType) {
		Message msg = Message.obtain(null, msgType);
		msg.replyTo = _messengerIn;
		sendMessage(msg);
	}

	XmppThread(XMPPTCPConnectionConfiguration config, Messenger messenger) {
		_xmppThread = new HandlerThread(TAG);
		_xmppThread.start();
		_messageHandler = new MessageHandler(_xmppThread.getLooper());
		_messengerIn = new Messenger(_messageHandler);
		_config = config;
		_messengerOut = messenger;

		_connection = new XMPPTCPConnection(_config);
		_presence = new Presence(Presence.Type.available);

		_roster = Roster.getInstanceFor(_connection);
		_roster.addRosterListener(new XmppRosterListener());
		_roster.setRosterLoadedAtLogin(true);
		// TODO: manual buddy acceptance
		// a PacketListener should be registered that listens for Presence packets that have a type of Presence.Type.subscribe
//		_roster.setSubscriptionMode(Roster.SubscriptionMode.manual);
		_roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);

		// Give our messenger to the other end
		Message msg = Message.obtain(null, MSG_REGISTER);
		msg.replyTo = _messengerIn;
		sendMessage(msg);
	}

	private class XmppRosterListener implements RosterListener {
		@Override
		public void entriesAdded(Collection<String> addresses) {

		}

		@Override
		public void entriesUpdated(Collection<String> addresses) {

		}

		@Override
		public void entriesDeleted(Collection<String> addresses) {

		}

		@Override
		public void presenceChanged(Presence presence) {
			Log.i(TAG, "Presence changed: from=" + presence.getFrom() + " status=" + presence.getStatus() + " mode=" + presence.getMode());
			Log.d(TAG, presence.toString());
		}
	}

	private void connect() {
		try {
			_connection.connect();
			_connection.login();
		} catch (Exception e) {
			Log.e(TAG, "Failed to connect");
			e.printStackTrace();
			sendMessage(MSG_CONNECT_FAIL);
			return;
		}
		sendMessage(MSG_CONNECTED);
	}

	private void disconnect() {
		_connection.disconnect();
	}

	private void setPresence(String status, String modeString) {
		Presence.Mode mode = null;
		try {
			mode = Presence.Mode.fromString(modeString);
		} catch (Exception e) {
			Log.e(TAG, "Invalid mode: " + modeString);
//			e.printStackTrace();
		}
		setPresence(status, mode);
	}

	private void setPresence(String status, Presence.Mode mode) {
//		Log.d(TAG, "setPresence " + status + " " + mode);

		if (!_connection.isAuthenticated()) {
			return;
		}

		boolean changed = false;
		if (mode != null && mode != _presence.getMode()) {
			_presence.setMode(mode);
			changed = true;
		}
		if (status != null && !status.equals(_presence.getStatus())) {
			_presence.setStatus(status);
			changed = true;
		}

		if (!changed) {
			return;
		}

		try {
			_connection.sendStanza(_presence);
		} catch (SmackException.NotConnectedException e) {
			Log.e(TAG, "setPresence() failed: Not connected");
			e.printStackTrace();
		}
	}

	private void listRoster() {
		Log.d(TAG, "Roster:");
		Collection<RosterEntry> rosterEntries = _roster.getEntries();
		for (RosterEntry rosterEntry : rosterEntries) {
			Log.d(TAG, rosterEntry.toString());
		}
		Log.d(TAG, "- End of roster -");
	}

	private void addRoster(String jid, String nick) {
		try {
//			_roster.createEntry("name@host.com", "nick", null);
			_roster.createEntry(jid, nick, null);
		} catch (Exception e) {
			Log.e(TAG, "Failed to add entry");
			e.printStackTrace();
			return;
		}
	}

//	public void stop() {
//		_xmppThread.quit();
//	}
}
