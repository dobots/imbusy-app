package nl.dobots.imbusy;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.packet.RosterPacket;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.TLSUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Bart van Vliet on 4-8-15.
 */
public class XmppService extends Service {
	private static final String TAG = XmppService.class.getCanonicalName();
	private static final String XMPP_DOMAIN = "dobots.customers.luna.net";
	private static final String XMPP_HOST = "dobots.customers.luna.net";
	private static final int XMPP_PORT = 5222;
	private static final int STATUS_POLL_DELAY = 500;
	private XmppThread _xmppThread = null;
	private Handler _handler;
	private XmppStatus _status = XmppStatus.DISCONNECTED;
	private XmppFriendList _friendList = new XmppFriendList();
	private XmppFriendList _friendRequestList = new XmppFriendList();

	private Set<XmppServiceListener> _listenerList = new HashSet<>();
	enum XmppStatus {
		DISCONNECTED,
		CONNECTING,
		CONNECTED,
		AUTHENTICATING,
		AUTHENTICATED
	}
	enum XmppError {
		CONNECT_FAILURE,
		AUTHORIZATION_FAILURE
	}

	enum XmppFriendEvent {
		ADDED,
		REMOVED,
		FRIEND_UPDATE,
		FRIEND_REQUEST
	}

	/** Binder given to users that bind to this service */
	public class XmppBinder extends Binder {
		XmppService getService() {
			return XmppService.this;
		}
	}
	private final IBinder _binder = new XmppBinder();

	/** Target we give to the xmpp thread to send messages to, handled by MessageHandler. */
	private final Messenger _messengerIn = new Messenger(new MessageHandler());
	private Messenger _messengerOut;


	@Override
	public void onCreate() {
		super.onCreate();

		SharedPreferences preferences = getSharedPreferences(ImBusyApp.getInstance().getPreferencesFile(), MODE_PRIVATE);
		String number = preferences.getString(ImBusyApp.PREFERENCE_PHONE_NUMBER, null);
		String password = preferences.getString(ImBusyApp.PREFERENCE_PASSWORD, null);

		XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
		configBuilder.setUsernameAndPassword(number, password);
		configBuilder.setServiceName(XMPP_DOMAIN);
		configBuilder.setHost(XMPP_HOST);
		configBuilder.setPort(XMPP_PORT);
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

		_xmppThread = new XmppThread(configBuilder, _messengerIn);


		_handler = new Handler();

//		//TODO: not by polling, but by broadcast message?
//		_handler.post(new Runnable() {
//			@Override
//			public void run() {
//				updateOwnStatus();
//				_handler.postDelayed(this, STATUS_POLL_DELAY);
//			}
//		});
	}

	public void addListener(XmppServiceListener listener) {
		_listenerList.add(listener);
	}

	public void removeListener(XmppServiceListener listener) {
		_listenerList.remove(listener);
	}

	private void sendToListeners(XmppStatus status) {
		for (XmppServiceListener listener : _listenerList) {
			listener.onConnectStatus(status);
		}
	}

	private void sendToListeners(XmppError error) {
		for (XmppServiceListener listener : _listenerList) {
			listener.onError(error);
		}
	}

	private void sendToListeners(XmppFriendEvent event, XmppFriend friend) {
		for (XmppServiceListener listener : _listenerList) {
			listener.onFriend(event, friend);
		}
	}


	/** To be used when username/password changed
	 */
	public void xmppLogin() {
		SharedPreferences preferences = getSharedPreferences(ImBusyApp.getInstance().getPreferencesFile(), MODE_PRIVATE);
		String number = preferences.getString(ImBusyApp.PREFERENCE_PHONE_NUMBER, null);
		String password = preferences.getString(ImBusyApp.PREFERENCE_PASSWORD, null);

		// TODO: are we sure that messages can't be seen by other apps?
		Message msg = Message.obtain(null, XmppThread.MSG_CONNECT);
		Bundle data = new Bundle();
		data.putString("username", number);
		data.putString("password", password);
		msg.setData(data);
		sendMessage(msg);
	}

	/** Adds a friend on XMPP, your friend will have to add you too, in order for it to have an effect */
	public void xmppAddFriend(String username, String nick) {
		Message msg = Message.obtain(null, XmppThread.MSG_ADD_FRIEND);
		Bundle data = new Bundle();
		data.putString("jid", username + "@" + XMPP_DOMAIN);
		data.putString("nick", nick);
		msg.setData(data);
		sendMessage(msg);
	}

	/** Removes a friend on XMPP, also denies this friend's subscription to you */
	public void xmppRemoveFriend(String jid) {
		Message msg = Message.obtain(null, XmppThread.MSG_REMOVE_FRIEND);
		Bundle data = new Bundle();
		data.putString("jid", jid);
		msg.setData(data);
		sendMessage(msg);
	}

	/** Answers a friend request, either accept or reject, else it will be asked again later (on login) */
	public void xmppAnswerFriendRequest(String username, boolean accept) {
		String jid = username + "@" + XMPP_DOMAIN;
		Message msg = Message.obtain(null, XmppThread.MSG_FRIEND_REQUEST_RESPONSE);
		Bundle data = new Bundle();
		data.putString("jid", jid);
		data.putBoolean("accept", accept);
		msg.setData(data);
		sendMessage(msg);
		if (_status == XmppStatus.AUTHENTICATED) {
			_friendRequestList.remove(jid);
		}
	}


	private void onXmppInitialized() {
		sendMessage(XmppThread.MSG_CONNECT);
	}

	private void onXmppConnected() {
		_status = XmppStatus.CONNECTED;
		sendToListeners(XmppStatus.CONNECTED);
	}

	private void onXmppConnectFail() {
		onXmppDisconnected();
		sendToListeners(XmppError.CONNECT_FAILURE);
	}

	private void onXmppAuthenticated() {
		_status = XmppStatus.AUTHENTICATED;
		sendToListeners(XmppStatus.AUTHENTICATED);
	}

	private void onXmppAuthenticateFail() {
		sendToListeners(XmppError.AUTHORIZATION_FAILURE);
	}

	private void onXmppDisconnected() {
		_status = XmppStatus.DISCONNECTED;
		sendToListeners(XmppStatus.DISCONNECTED);
	}

	private void onXmppFriendAdded(String jid, String nick, String presenceMode, String subscriptionType) {
//		if (jid == null || nick == null || presenceMode == null) {
		if (jid == null || presenceMode == null || subscriptionType == null) {
			return;
		}

		Presence.Mode mode = XmppThread.getMode(presenceMode);
		RosterPacket.ItemType itemType = XmppThread.getSubscriptionType(subscriptionType);
		XmppFriend friend = new XmppFriend(jid, nick, mode, itemType);
		_friendList.add(friend);

		sendToListeners(XmppFriendEvent.ADDED, friend);

//		String number = getNumber(jid);
//		Status status = getStatus(XmppThread.getMode(presenceMode));
//		ImBusyApp.getInstance().getContactList().add(new PhoneContact(number, nick, status));
	}

	private void onXmppFriendRemoved(String jid) {
		if (jid == null) {
			return;
		}
		XmppFriend friend = _friendList.get(jid);
		if (friend != null) {
			_friendList.remove(jid);
			sendToListeners(XmppFriendEvent.REMOVED, friend);
		}

//		String number = getNumber(jid);
//		ImBusyApp.getInstance().getContactList().remove(number);
	}

	private void onXmppFriendRequest(String jid) {
		if (jid == null) {
			return;
		}
		XmppFriend friend = new XmppFriend(jid, null, null, null);
		_friendRequestList.add(friend);
		sendToListeners(XmppFriendEvent.FRIEND_REQUEST, friend);
	}

	private void onXmppFriendUpdate(String jid, String presenceMode, String subscriptionTypeString) {
		XmppFriend friend = _friendList.get(jid);
		if (friend != null) {
			boolean updated = false;
			if (presenceMode != null) {
				Presence.Mode mode = XmppThread.getMode(presenceMode);
				if (!friend.getMode().equals(mode)) {
					updated = true;
					friend.setMode(mode);
				}
			}

			if (subscriptionTypeString != null) {
				RosterPacket.ItemType subscriptionType = XmppThread.getSubscriptionType(subscriptionTypeString);
				if (!friend.getSubscriptionType().equals(subscriptionType)) {
					updated = true;
					friend.setSubscriptionType(subscriptionType);
				}
			}

			if (updated) {
				sendToListeners(XmppFriendEvent.FRIEND_UPDATE, friend);
			}
		}


//		PhoneContactList contacts = ImBusyApp.getInstance().getContactList();
//		String number = getNumber(jid);
//		if (contacts.containsKey(number)) {
//			contacts.get(number).setStatus(getStatus(mode));
//			Log.d(TAG, "Update status to " + getStatus(mode));
//		}
	}

	public void setPresence(Presence presence) {
		Message msg = Message.obtain(null, XmppThread.MSG_SET_PRESENCE);
		Bundle data = new Bundle();
		String modeString = presence.getMode().name();
		data.putString("mode", modeString);
		String status = presence.getStatus();
		data.putString("status", status);
		msg.setData(data);
		sendMessage(msg);
	}

	public XmppFriendList getFriendList() {
		return _friendList;
	}

	public XmppFriendList getFriendRequestList() {
		return _friendRequestList;
	}

	//	private void updateOwnStatus() {
//		if (ImBusyApp.getInstance() == null) {
//			return;
//		}
//
//		Status status = ImBusyApp.getInstance().getStatus();
//		if (status == null) {
//			return;
//		}
//		String xmppModeText = getMode(status).name();
//		Message msg = Message.obtain(null, XmppThread.MSG_SET_PRESENCE);
//		Bundle data = new Bundle();
////		data.putString("status", "");
//		data.putString("mode", xmppModeText);
//		msg.setData(data);
//		sendMessage(msg);
//	}

	@Override
	public IBinder onBind(Intent intent) {
		return _binder;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		sendMessage(XmppThread.MSG_DISCONNECT);
		_handler.removeCallbacksAndMessages(null);
		if (_xmppThread != null) {
			_xmppThread.stop();
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
				case XmppThread.MSG_AUTHENTICATED: {
					onXmppAuthenticated();
					break;
				}
				case XmppThread.MSG_AUTHENTICATE_FAIL: {
					onXmppAuthenticateFail();
					break;
				}
				case XmppThread.MSG_CONNECT_FAIL: {
					onXmppConnectFail();
					break;
				}
				case XmppThread.MSG_DISCONNECTED: {
					onXmppDisconnected();
					break;
				}

				case XmppThread.MSG_FRIEND_ADDED: {
					String jid = msg.getData().getString("jid");
					String nick = msg.getData().getString("nick");
					String mode = msg.getData().getString("mode");
					String subscriptionType = msg.getData().getString("subscriptionType");
					onXmppFriendAdded(jid, nick, mode, subscriptionType);
					break;
				}
				case XmppThread.MSG_FRIEND_REMOVED: {
					String jid = msg.getData().getString("jid");
					onXmppFriendRemoved(jid);
					break;
				}
				case XmppThread.MSG_FRIEND_REQUEST: {
					String jid = msg.getData().getString("jid");
					onXmppFriendRequest(jid);
					break;
				}
				case XmppThread.MSG_FRIEND_UPDATE: {
					String jid = msg.getData().getString("jid");
					String mode = msg.getData().getString("mode");
					String subscriptionType = msg.getData().getString("subscriptionType");
					onXmppFriendUpdate(jid, mode, subscriptionType);
					break;
				}
				default: {
					super.handleMessage(msg);
				}
			}
		}
	}
}
