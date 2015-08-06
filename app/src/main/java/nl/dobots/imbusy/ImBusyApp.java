package nl.dobots.imbusy;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import nl.dobots.bluenet.extended.BleExt;
import nl.dobots.bluenet.extended.structs.BleDevice;
import nl.dobots.bluenet.extended.structs.BleDeviceMap;

/**
 * Created by Bart van Vliet on 24-7-15.
 */

public class ImBusyApp extends Application {
	private static final String TAG = ImBusyApp.class.getCanonicalName();
	private static ImBusyApp _instance;
	private Context _context;
	private static final String DATABASE_NAME = "ImBusyDataBase";
	private static final int DATABASE_VERSION = 1;

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

	private Status _status;

	public static ImBusyApp getInstance() {
		return _instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		_instance = this;
		_context = this.getApplicationContext();

		_handler = new Handler();

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
		this.startService(new Intent(this, XmppService.class));
	}

	/** Stop all services
	 */
	public void stop() {
		_storedDeviceList.save();
		stopService(new Intent(this, StatusPopupService.class));
		stopService(new Intent(this, CallStateService.class));
		stopService(new Intent(this, BleScanService.class));
		stopService(new Intent(this, XmppService.class));
		_ble = null;
//		System.exit(0);
//		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public void onOutgoingCall(String number) {
		Log.i(TAG, "---------------------------");
		Log.i(TAG, "Outgoing call to " + number);
		Log.i(TAG, "---------------------------");
		startService(new Intent(this, StatusPopupService.class));
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
}
