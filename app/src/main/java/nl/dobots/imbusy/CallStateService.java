package nl.dobots.imbusy;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by Bart van Vliet on 24-7-15.
 *
 * Based on: http://www.codeproject.com/Articles/548416/Detecting-incoming-and-outgoing-phone-calls-on-And
 */
public class CallStateService extends Service {
	private static final String TAG = CallStateService.class.getCanonicalName();
	private TelephonyManager _telephonyManager;
	private Context _context;
	private OutgoingCallListener _outgoingCallListener;
	private CallStateListener _callStateListener;

	private class CallStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// Incoming call, state goes: 1 -> answer call -> 2 -> hang up -> 0
			// Outgoing call, state goes: 2 -> hang up -> 0
			switch (state) {
				case TelephonyManager.CALL_STATE_RINGING: {
					Log.d(TAG, "Call state ringing (" + state + ") incoming nr=" + incomingNumber);
					break;
				}
				case TelephonyManager.CALL_STATE_IDLE: {
					Log.d(TAG, "Call state idle (" + state + ") incoming nr=" + incomingNumber);
					ImBusyApp.getInstance().onHangUp();
					break;
				}
				case TelephonyManager.CALL_STATE_OFFHOOK: {
					Log.d(TAG, "Call state off hook (" + state + ") incoming nr=" + incomingNumber);
					break;
				}
			}
		}
	}
	private class OutgoingCallListener extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			Log.d(TAG, "Outgoing call to " + number);
			ImBusyApp.getInstance().onOutgoingCall(number);
//			_context.sendBroadcast();
		}
	}


	@Override
	public void onCreate() {
		super.onCreate();

		_outgoingCallListener = new OutgoingCallListener();
		_callStateListener = new CallStateListener();

		_context = this.getApplicationContext();
		_telephonyManager = (TelephonyManager)_context.getSystemService(Context.TELEPHONY_SERVICE);
		_telephonyManager.listen(_callStateListener, PhoneStateListener.LISTEN_CALL_STATE);

		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
		_context.registerReceiver(_outgoingCallListener, intentFilter);
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

		// Stop listeners
		_context.unregisterReceiver(_outgoingCallListener);
		_telephonyManager.listen(_callStateListener, PhoneStateListener.LISTEN_NONE);

//		// Remove all callbacks and messages that were posted
//		_handler.removeCallbacksAndMessages(null);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
