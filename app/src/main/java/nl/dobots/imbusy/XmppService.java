package nl.dobots.imbusy;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.TLSUtils;

/**
 * Created by Bart van Vliet on 4-8-15.
 */
public class XmppService extends Service {
	private static final String TAG = XmppService.class.getCanonicalName();
	private static final int STATUS_POLL_DELAY = 500;
	private XmppThread _xmppThread = null;
	private Handler _handler;
	private XmppFriendList _friendList = new XmppFriendList();

	/** Target we give to the xmpp thread to send messages to, handled by MessageHandler. */
	private final Messenger _messengerIn = new Messenger(new MessageHandler());
	private Messenger _messengerOut;


	/** Handler of incoming messages from xmpp thread */
	private class MessageHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case XmppThread.MSG_REGISTER: {
					Log.i(TAG, "Register xmpp thread messenger");
					_messengerOut = msg.replyTo;
					onXmppInitialized();
					break;
				}
				case XmppThread.MSG_CONNECTED: {
					onXmppConnected();
					break;
				}
				case XmppThread.MSG_FRIEND_ADDED: {
					String jid = msg.getData().getString("jid");
					String nick = msg.getData().getString("nick");
					String mode = msg.getData().getString("mode");
					onFriendAdded(jid, nick, mode);
					break;
				}
				case XmppThread.MSG_FRIEND_REMOVED: {
					String jid = msg.getData().getString("jid");
					onFriendRemoved(jid);
					break;
				}
				case XmppThread.MSG_STATUS: {
					String jid = msg.getData().getString("jid");
					String mode = msg.getData().getString("mode");
					onXmppFriendStatusUpdate(jid, mode);
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
		sendMessage(msg);
	}


	@Override
	public void onCreate() {
		super.onCreate();
		XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
		configBuilder.setUsernameAndPassword("name", "password");
		configBuilder.setServiceName("domain");
//		configBuilder.setHost("host");
		configBuilder.setPort(1234);
		configBuilder.setDebuggerEnabled(true);

		// TODO: make sure we use a secure connection!
//		configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);

		// Need this to accept self signed certificate
		// TODO: use these examples:
		// https://community.igniterealtime.org/thread/56000
		// http://www.codeproject.com/Articles/826045/Android-security-Implementation-of-Self-signed-SSL
		// http://randomizedsort.blogspot.nl/2010/09/step-to-step-guide-to-programming.html
		// https://blog.synyx.de/2010/06/android-and-self-signed-ssl-certificates/
		try {
			TLSUtils.acceptAllCertificates(configBuilder);
		} catch (Exception e) {
			Log.e(TAG, "Could not configure TLS");
			e.printStackTrace();
			return;
		}
		TLSUtils.disableHostnameVerificationForTlsCertificicates(configBuilder);



		// Set a resource name
		String resource = "android_";
		resource += android.os.Build.MODEL.replaceAll(" ", "_");
//		int postfix = new Random().nextInt(999) + 1; // number from 1 to 999, seeded by current time
//		resource += "_" + postfix;
		configBuilder.setResource(resource);

		XMPPTCPConnectionConfiguration config = configBuilder.build();
		_xmppThread = new XmppThread(config, _messengerIn);


		_handler = new Handler();

		//TODO: not by polling, but by broadcast message?
		_handler.post(new Runnable() {
			@Override
			public void run() {
				updateStatus();
				_handler.postDelayed(this, STATUS_POLL_DELAY);
			}
		});
	}

	private void onXmppInitialized() {
		sendMessage(XmppThread.MSG_CONNECT);
	}

	private void onXmppConnected() {
//		sendMessage(XmppThread.MSG_GET_FRIENDS); // Don't have the roster here yet..

	}

	private void onFriendAdded(String jid, String nick, String presenceMode) {
//		if (jid == null | nick == null | presenceMode == null) {
		if (jid == null | presenceMode == null) {
			return;
		}

		Presence.Mode mode = XmppThread.getMode(presenceMode);
		XmppFriend friend = new XmppFriend(jid, nick, mode);
		_friendList.add(friend);

		Status status = getStatus(mode);
		ImBusyApp.getInstance().getContactList().add(new PhoneContact(friend.getNumber(), friend.getNick(), status));
	}

	private void onFriendRemoved(String jid) {
		if (jid == null) {
			return;
		}
		_friendList.remove(jid);
	}

	private void onXmppFriendStatusUpdate(String jid, String presenceMode) {
		if (_friendList.containsKey(jid)) {
			_friendList.get(jid).setMode(XmppThread.getMode(presenceMode));
			Log.d(TAG, "Update mode to " + presenceMode);
		}

		Presence.Mode mode = XmppThread.getMode(presenceMode);
		PhoneContactList contacts = ImBusyApp.getInstance().getContactList();
		String number = getNumber(jid);
		if (contacts.containsKey(number)) {
			contacts.get(number).setStatus(getStatus(mode));
			Log.d(TAG, "Update status to " + getStatus(mode));
		}
	}

	private void updateStatus() {
		if (ImBusyApp.getInstance() == null) {
			return;
		}

		Status status = ImBusyApp.getInstance().getStatus();
		if (status == null) {
			return;
		}
		String xmppModeText = getMode(status).name();
		Message msg = Message.obtain(null, XmppThread.MSG_SET_STATUS);
		Bundle data = new Bundle();
//		data.putString("status", "");
		data.putString("mode", xmppModeText);
		msg.setData(data);
		sendMessage(msg);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		sendMessage(XmppThread.MSG_DISCONNECT);
//		if (_xmppThread != null) {
//			_xmppThread.stop();
//		}
	}

	/** Helper function to convert xmpp presence mode to status */
	public static final Status getStatus(Presence.Mode mode) {
		switch (mode) {
			case away:
				return Status.BUSY;
			case xa:
				return Status.BUSY;
			case dnd:
				return Status.BUSY;
			case chat:
			case available:
			default:
				return Status.AVAILABLE;
		}
	}

	/** Helper function to convert status to xmpp presence mode */
	public static final Presence.Mode getMode(Status status) {
		switch (status) {
			case BUSY:
				return Presence.Mode.dnd;
			case AVAILABLE:
			default:
				return Presence.Mode.available;
		}
	}

	public static String getNumber(String jid) {
		if (!jid.contains("@")) {
			return null;
		}
		String[] split = jid.split("@");
		return split[0];
	}
}
