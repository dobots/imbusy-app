package nl.dobots.imbusy;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.jivesoftware.smack.packet.Presence;

import nl.dobots.bluenet.extended.BleExt;
import nl.dobots.bluenet.extended.structs.BleDevice;
import nl.dobots.bluenet.extended.structs.BleDeviceMap;

/**
 * Created by Bart van Vliet on 24-7-15.
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
//		this.startService(new Intent(this, BleScanService.class));
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
		}
//		_xmppService = null;

		_ble = null;
//		System.exit(0);
//		android.os.Process.killProcess(android.os.Process.myPid());
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
		}

		@Override
		public void onError(XmppService.XmppError error) {

		}

		@Override
		public void onPresence(Presence presence) {

		}
	};

	public XmppService.XmppStatus getXmppStatus() {
		return _xmppStatus;
	}

	public void setStatus(Status status) {
		Log.d(TAG, "Changed status to " + status);
		_handler.removeCallbacks(_setAvailable);
		_status = status;
		_handler.postDelayed(_setAvailable, 10000); // TODO: magic nr
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
}
