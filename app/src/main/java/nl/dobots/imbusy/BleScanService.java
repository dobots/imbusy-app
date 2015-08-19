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

public class BleScanService extends Service {
	private static final String TAG = BleScanService.class.getCanonicalName();
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
//			Log.d(TAG, "Start endless scan");
			_ble.startIntervalScan(_deviceCallback);
			_handler.postDelayed(_stopScanRunnable, Config.BLE_SCAN_INTERVAL);
		}
	};
	private Runnable _stopScanRunnable = new Runnable() {
		@Override
		public void run() {
//			Log.d(TAG, "Stop endless scan");
			_ble.stopScan(new IStatusCallback() {
				@Override
				public void onSuccess() {
					_handler.postDelayed(_startScanRunnable, Config.BLE_SCAN_PAUSE);
				}
				@Override
				public void onError(int error) {
					_handler.postDelayed(_startScanRunnable, Config.BLE_SCAN_PAUSE);
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

		startIntervalScan();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//The service will at this point continue running until Context.stopService() or stopSelf() is called
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		if (_ble != null) {
			_ble.stopScan(new IStatusCallback() {
				@Override
				public void onSuccess() {

				}

				@Override
				public void onError(int error) {

				}
			});
		}

		// Remove all callbacks and messages that were posted
		_handler.removeCallbacksAndMessages(null);
	}

	public void startScan() {
		_ble.startScan(_deviceCallback);
	}

	public void stopScan() {
		_ble.stopScan(new IStatusCallback() {
			@Override
			public void onSuccess() {

			}

			@Override
			public void onError(int error) {

			}
		});
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


	// TODO: implement this
//	//BLE Adapter to remember user not to turn BLE off
//	BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
//	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			String action = intent.getAction();
//			// It means the user has changed his bluetooth state.
//			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
//				if (btAdapter.getState() == BluetoothAdapter.STATE_TURNING_OFF) {
//					triggerNotification("Presence can not work without BLE ! Please turn the bluetooth back on when you want to check in or out.");
//					return;
//				}
//			}
//		}
//	};
//
//	private void triggerNotification(String s) {
//		final Intent intent = new Intent(this, startingActivity.class);
//		final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
//		CharSequence message = s;
//		NotificationManager notificationManager;
//		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//		Notification notification = new Notification.Builder(this)
//				.setContentTitle("Presence")
//				.setContentText(message)
//				.setSmallIcon(R.mipmap.ic_launcher)
//				.setContentIntent(contentIntent)
//				.setDefaults(Notification.DEFAULT_SOUND)
//				.setLights(Color.BLUE, 500, 1000)
//				.setAutoCancel(true)
//				.build();
//		notificationManager.notify(1011, notification);
//	}
}
