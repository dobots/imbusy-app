package nl.dobots.imbusy;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.jivesoftware.smack.packet.Presence;

import java.util.HashSet;
import java.util.Set;

import nl.dobots.bluenet.extended.BleExt;
import nl.dobots.bluenet.extended.structs.BleDevice;
import nl.dobots.bluenet.extended.structs.BleDeviceMap;

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
 * Created on 24-7-15
 *
 * @author Bart van Vliet
 */


// TODO: bind to services to send stuff to services
// TODO: use event listeners instead of calling ImBusyApp.function() from services.
// TODO: use local broadcasts to send stuff to UI? <-- listener is fine too (but synchronous)

public class ImBusyApp extends Application {
	private static final String TAG = ImBusyApp.class.getCanonicalName();
	private static ImBusyApp _instance;
	private Context _context;
	private static final String DATABASE_NAME = "ImBusyDataBase";
	private static final int DATABASE_VERSION = 1;
	private static final String PREFERENCES_FILE = "ImBusyPreferences";
	public static final String PREFERENCE_PHONE_NUMBER = "phoneNumber";
	public static final String PREFERENCE_PASSWORD = "password";

	private NotificationManager _notificationManager;
	private NotificationCompat.Builder _notificationBuilder;

	private BleExt _ble;
	private StoredBleDeviceList _storedDeviceList;
	private Handler _handler;
	Runnable _setAvailable = new Runnable() {
		@Override
		public void run() {
			Log.d(TAG, "Changed status to " + Status.AVAILABLE);
			_status = Status.AVAILABLE;
		}
	};

	private SharedPreferences _preferences;
	private Status _status;
	private XmppService.XmppStatus _xmppStatus;
//	private String _phoneNumber;
	private PhoneContactList _contactList = new PhoneContactList();
	private Set<ImBusyListener> _listenerList = new HashSet<>();

	private XmppService _xmppService = null;
	private ServiceConnection _xmppServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			_xmppService = ((XmppService.XmppBinder)service).getService();
			_xmppService.addListener(_xmppListener);
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			_xmppService = null;
		}
	};

	public static ImBusyApp getInstance() {
		return _instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		_instance = this;
		_context = this.getApplicationContext();

		_handler = new Handler();

		_notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		_preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
//		_phoneNumber = _preferences.getString("phoneNumber", "");

		_storedDeviceList = new StoredBleDeviceList(_context);
		_storedDeviceList.load();

		start();
	}

	public void start() {
		_status = Status.AVAILABLE;
		if (_ble == null) {
			_ble = new BleExt();
		}
		this.startService(new Intent(this, CallStateService.class));
		this.startService(new Intent(this, BleScanService.class));
		if (_xmppService == null) {
			bindService(new Intent(this, XmppService.class), _xmppServiceConnection, Context.BIND_AUTO_CREATE);
		}
	}

	/** Stop all services
	 */
	public void stop() {
		_storedDeviceList.save();
		stopService(new Intent(this, StatusPopupService.class));
		stopService(new Intent(this, CallStateService.class));
		stopService(new Intent(this, BleScanService.class));
		if (_xmppService != null) {
			_xmppService.removeListener(_xmppListener);
			unbindService(_xmppServiceConnection);
			_xmppService = null;
		}
//		_xmppService = null;

		_ble = null;
//		System.exit(0);
//		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public void addListener(ImBusyListener listener) {
		_listenerList.add(listener);
	}

	public void removeListener(ImBusyListener listener) {
		_listenerList.remove(listener);
	}

	public void sendToListeners(Status status) {
		for (ImBusyListener listener : _listenerList) {
			listener.onStatus(status);
		}
	}

	/** Should be called when an activity is opened */
//	public void onActivityOpen(Activity activity) {
	public void onActivityOpen(Class activityClass) {
		_notificationManager.cancel(Config.IMBUSY_NOTIFICATION_ID_FRIEND_REQUEST);
	}

	/** Should be called when an activity is closed */
	public void onActivityClose(Class activityClass) {

	}

	public void onOutgoingCall(String number) {
		Log.i(TAG, "---------------------------");
		Log.i(TAG, "Outgoing call to " + number);
		Log.i(TAG, "---------------------------");
		String fixedNumber = new String(number);
		fixedNumber = fixedNumber.replaceAll(" ","");
		fixedNumber = fixedNumber.replaceAll("\\+", "00");
		if (_contactList.containsKey(fixedNumber) && _contactList.get(fixedNumber).getStatus() == Status.BUSY) {
			Log.i(TAG, fixedNumber + " is busy! Show popup");
			startService(new Intent(this, StatusPopupService.class));
		}
		else Log.d(TAG, fixedNumber + " is not a contact or is not busy");
	}

	/** Phone goes idle, remove the status popup
	 */
	public void onHangUp() {
		stopService(new Intent(this, StatusPopupService.class));
	}

	public void onScannedDevice(BleDevice device) {
		if (device == null) {
			return;
		}
		Log.d(TAG, "Scanned " + device.getAddress() + " (" + device.getRssi() + ") " + device.getName());
//		Log.d(TAG, "scanned device list size = " + _ble.getDeviceMap().size());
		if (_storedDeviceList.isClose(device.getAddress(), device.getRssi())) {
			setStatus(Status.BUSY);
		}
	}

	final XmppServiceListener _xmppListener = new XmppServiceListener() {
		@Override
		public void onConnectStatus(XmppService.XmppStatus status) {
			_xmppStatus = status;
			switch (status) {
				case AUTHENTICATED:{
					// After authentication, all numbers are added again, so we start with a clean list
					_contactList.clear();
					break;
				}
			}
		}

		@Override
		public void onError(XmppService.XmppError error) {

		}

		@Override
		public void onFriend(XmppService.XmppFriendEvent event, XmppFriend friend) {
			String number = getNumber(friend.getJid());
			switch (event) {
				case ADDED:{
					String name = getContactName(getNumber(friend.getJid()));
//					if (friend.getNick() == null) {
//						String name = getContactName(getNumber(friend.getJid()));
						friend.setNick(name);
//					}
					_contactList.add(new PhoneContact(number, name));
					break;
				}
				case REMOVED: {
					_contactList.remove(number);
					break;
				}
				case FRIEND_UPDATE: {
					_contactList.get(number).setStatus(getStatus(friend.getMode()));
					break;
				}
				case FRIEND_REQUEST:{
					String name = getContactName(number);
					String text = name + " (" + number + ") wants to see your status.";

					Intent contentIntent = new Intent(_context, MainActivity.class);
					contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
					PendingIntent piContent = PendingIntent.getActivity(_context, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

					_notificationBuilder = new NotificationCompat.Builder(_context);
					_notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
					_notificationBuilder.setContentTitle("Friend request");
					_notificationBuilder.setContentText(text);
					_notificationBuilder.setContentIntent(piContent);
					_notificationBuilder.setAutoCancel(true);
					_notificationBuilder.setDefaults(Notification.DEFAULT_SOUND);
					_notificationBuilder.setLights(Color.BLUE, 500, 1000);
					_notificationManager.notify(Config.IMBUSY_NOTIFICATION_ID_FRIEND_REQUEST, _notificationBuilder.build());
				}
			}
		}
	};

	public XmppService.XmppStatus getXmppStatus() {
		return _xmppStatus;
	}

	public void setStatus(Status status) {
		Log.d(TAG, "Changed status to " + status);
		_status = status;
		sendToListeners(status);
		_handler.removeCallbacks(_setAvailable);
		_handler.postDelayed(_setAvailable, Config.BUSY_TIMEOUT);
	}

	public StoredBleDeviceList getStoredDeviceList() {
		return _storedDeviceList;
	}

	public BleExt getBle() {
		return _ble;
	}

	public BleDeviceMap getDeviceMap() {
		if (_ble != null) {
			return _ble.getDeviceMap();
		}
		return null;
	}

	public static String getDatabaseName() {
		return DATABASE_NAME;
	}

	public static int getDatabaseVersion() {
		return DATABASE_VERSION;
	}

	public Status getStatus() {
		return _status;
	}

	public PhoneContactList getContactList() {
		return _contactList;
	}

	public static String getPreferencesFile() {
		return PREFERENCES_FILE;
	}

	//	public String getOwnNumber() { return _phoneNumber; }



	/** Helper function to convert xmpp presence mode to status */
	public static final Status getStatus(Presence.Mode mode) {
		switch (mode) {
			case away:
				return nl.dobots.imbusy.Status.BUSY;
			case xa:
				return nl.dobots.imbusy.Status.BUSY;
			case dnd:
				return nl.dobots.imbusy.Status.BUSY;
			case chat:
			case available:
			default:
				return nl.dobots.imbusy.Status.AVAILABLE;
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

	public final String getStatusText(Status status) {
		switch (status) {
			case BUSY:
				return getString(R.string.status_busy);
			case AVAILABLE:
			default:
				return getString(R.string.status_available);
		}
	}

	public static String getXmppUsername(String phoneNumber) {
		return phoneNumber;
	}

	/** Helper function to get the phone number from a jid */
	public static String getNumber(String jid) {
		return XmppThread.getUsername(jid);
	}

	/** Retrieves the name as stored in the phones address book, given a phone number */
	public String getContactName(String phoneNumber) {
		String[] projection = new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID };
		Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
		String contactName = "";
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
			}
			cursor.close();
		}
		return contactName;
	}
}
