package nl.dobots.imbusy;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import nl.dobots.bluenet.callbacks.IBleDeviceCallback;
import nl.dobots.bluenet.callbacks.IStatusCallback;
import nl.dobots.bluenet.extended.BleExt;
import nl.dobots.bluenet.extended.structs.BleDevice;

/**
 * Created by vliedel on 24-7-15.
 */
public class BleScanService extends Service {
	private String TAG = BleScanService.class.getCanonicalName();
	private BleExt _ble;
	private Handler _handler = new Handler();

	private IBleDeviceCallback _deviceCallback = new IBleDeviceCallback() {
		@Override
		public void onSuccess(BleDevice device) {
//				Log.i(TAG, device.getAddress());
			ImBusyApp.getInstance().onScannedDevice(device);
		}

		@Override
		public void onError(int error) {

		}
	};

	private Runnable _startScanRunnable = new Runnable() {
		@Override
		public void run() {
			Log.d(TAG, "Start endless scan");
			_ble.startScan(_deviceCallback);
			_handler.postDelayed(_stopScanRunnable, 3000);
		}
	};
	private Runnable _stopScanRunnable = new Runnable() {
		@Override
		public void run() {
			Log.d(TAG, "Stop endless scan");
			_ble.stopScan(new IStatusCallback() {
				@Override
				public void onSuccess() {
					_handler.postDelayed(_startScanRunnable, 1000);
				}
				@Override
				public void onError(int error) {
					_handler.postDelayed(_startScanRunnable, 1000);
				}
			});
		}
	};


	@Override
	public void onCreate() {
		super.onCreate();
		_ble = ImBusyApp.getInstance().getBle();

		_ble.init(this.getApplicationContext(), new IStatusCallback() {
			@Override
			public void onSuccess() {

			}
			@Override
			public void onError(int error) {

			}
		});
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//The service will at this point continue running until Context.stopService() or stopSelf() is called

		startIntervalScan();
//		_ble.startEndlessScan(new IBleDeviceCallback() {
//			@Override
//			public void onSuccess(BleDevice device) {
////				Log.i(TAG, device.getAddress());
//				ImBusyApp.getInstance().onScannedDevice(device);
//			}
//
//			@Override
//			public void onError(int error) {
//
//			}
//		});
//		_ble.startEndlessScan(new IDataCallback() {
//			@Override
//			public void onData(JSONObject json) {
//				Log.i(TAG, json.toString());
//				try {
//					BleDevice dev = new BleDevice(json);
//				} catch (JSONException e) {
//					Log.e(TAG, e.toString());
//				}
//			}
//
//			@Override
//			public void onError(int error) {
//
//			}
//		});

		return Service.START_STICKY;
	}

	public void startIntervalScan() {
		Log.i(TAG, "Starting interval scan");
		_handler.post(_startScanRunnable);
	}

	public void stopIntervalScan() {
		_handler.removeCallbacks(_startScanRunnable);
		_handler.removeCallbacks(_stopScanRunnable);
		_ble.stopScan(new IStatusCallback() {
			@Override
			public void onSuccess() {
				Log.i(TAG, "Stopped interval scan");
			}
			@Override
			public void onError(int error) {
				Log.i(TAG, "Failed to stop interval scan");
			}
		});
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
