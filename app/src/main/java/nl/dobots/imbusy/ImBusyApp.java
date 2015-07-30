package nl.dobots.imbusy;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import nl.dobots.bluenet.extended.BleExt;
import nl.dobots.bluenet.extended.structs.BleDevice;

/**
 * Created by Bart van Vliet on 24-7-15.
 */

public class ImBusyApp extends Application {
	private static final String TAG = ImBusyApp.class.getCanonicalName();
	private static ImBusyApp _instance;
	private Context _context;

	private BleExt _ble = new BleExt();
	private StoredBleDeviceList _bleDeviceList = new StoredBleDeviceList();
	private Handler _statusTimeoutHandler;
	Runnable _setAvailable = new Runnable() {
		@Override
		public void run() {
			Log.d(TAG, "Changed status to " + Status.AVAILABLE);
			_status = Status.AVAILABLE;
		}
	};


	public enum Status {
		AVAILABLE,
		BUSY
	}
	private Status _status;

	public static ImBusyApp getInstance() {
		return _instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		_instance = this;
		_context = this.getApplicationContext();

		_statusTimeoutHandler = new Handler();

		_bleDeviceList = new StoredBleDeviceList();
		_bleDeviceList.add(new StoredBleDevice("C0:9A:E5:1F:95:23", "whosin"));
		_bleDeviceList.add(new StoredBleDevice("E0:0B:E5:5C:6B:9B", "bart"));

		this.startService(new Intent(this, CallStateService.class));
		this.startService(new Intent (this, BleScanService.class));
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
		Log.d(TAG, "Scanned " + device.getAddress() + " (" + device.getRssi() + ") " + device.getName());
		Log.d(TAG, "scanned device list size = " + _ble.getDeviceMap().size());
		if (_bleDeviceList.isClose(device.getAddress(), device.getRssi())) {
			setStatus(Status.BUSY);
		}
	}

	public void setStatus(Status status) {
		Log.d(TAG, "Changed status to " + status);
		_statusTimeoutHandler.removeCallbacks(_setAvailable);
		_status = status;
		_statusTimeoutHandler.postDelayed(_setAvailable, 10000); // TODO: magic nr
	}

	public StoredBleDeviceList getBleDeviceList() {
		return _bleDeviceList;
	}

	public BleExt getBle() {
		return _ble;
	}
}
