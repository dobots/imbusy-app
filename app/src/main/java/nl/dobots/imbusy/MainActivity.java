package nl.dobots.imbusy;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jivesoftware.smack.packet.Presence;

import java.util.ArrayList;

import nl.dobots.bluenet.extended.structs.BleDevice;
import nl.dobots.bluenet.extended.structs.BleDeviceMap;


public class MainActivity extends ActionBarActivity {
	private static final String TAG = MainActivity.class.getCanonicalName();
	private static final int STATUS_POLL_DELAY = 500;
	private Context _context;
	private Handler _handler;
	private Button _stopButton;
	private Button _loginButton;

	private XmppService _xmppService = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_context = this;
		setContentView(R.layout.activity_main);
		ImBusyApp.getInstance().start();

		SharedPreferences preferences = getSharedPreferences(ImBusyApp.getInstance().getPreferencesFile(), MODE_PRIVATE);
		String number = preferences.getString(ImBusyApp.PREFERENCE_PHONE_NUMBER, "");
		if (number.isEmpty()) {
			login();
		}

		if (_xmppService == null) {
			bindService(new Intent(this, XmppService.class), _xmppServiceConnection, Context.BIND_AUTO_CREATE);
		}

		updateStatus();
		updateClosestDevice();

		_handler = new Handler();

		//TODO: not by polling, but by broadcast message?
		_handler.post(new Runnable() {
			@Override
			public void run() {
				updateStatus();
				updateClosestDevice();
				_handler.postDelayed(this, STATUS_POLL_DELAY);
			}
		});

		initButtons();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		if (_xmppService != null) {
			_xmppService.removeListener(_xmppListener);
			unbindService(_xmppServiceConnection);
		}
		// Remove all callbacks and messages that were posted
		_handler.removeCallbacksAndMessages(null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		switch(id) {
			case R.id.device_settings: {
				startActivity(new Intent(this, DeviceSettingsActivity.class));
				return true;
			}
			case R.id.device_selection: {
				startActivity(new Intent(this, DeviceSelectActivity.class));
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	private void initButtons(){
		_stopButton = (Button) findViewById(R.id.stopButton);
		_stopButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
				ImBusyApp.getInstance().stop();
			}
		});
		_loginButton = (Button) findViewById(R.id.loginButton);
		_loginButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				login();
			}
		});
	}

	public void setStatus(Status status) {
		if (status == null) {
			return;
		}
		String text = getString(R.string.status_prefix) + " ";
		switch (status) {
			case AVAILABLE:
				text += getString(R.string.status_available);
				break;
			case BUSY:
				text += getString(R.string.status_busy);
				break;
		}
		final TextView statusTextView = (TextView) findViewById(R.id.status_text);
		statusTextView.setText(text);
	}

	private void updateStatus() {
		if (ImBusyApp.getInstance() != null) {
			setStatus(ImBusyApp.getInstance().getStatus());
		}
	}

	private void updateClosestDevice() {
		final TextView closestDeviceTextView = (TextView) findViewById(R.id.closest_device);
		String text = getResources().getString(R.string.closest_device_prefix);
		BleDeviceMap deviceMap = ImBusyApp.getInstance().getDeviceMap();
		if (deviceMap != null) {
			ArrayList<BleDevice> deviceList = deviceMap.getSortedList();
			if (deviceList != null && deviceList.size() > 0) {
				BleDevice closestDevice = deviceList.get(0);
				text += " " + closestDevice.getName() + " (" + closestDevice.getRssi() + " dB)";
			}
		}
		closestDeviceTextView.setText(text);
	}

	private void login() {
		startActivity(new Intent(this, LoginActivity.class));
	}



	private final ServiceConnection _xmppServiceConnection = new ServiceConnection() {
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
	private final XmppServiceListener _xmppListener = new XmppServiceListener() {
		@Override
		public void onConnectStatus(XmppService.XmppStatus status) {
			switch (status) {
				case AUTHENTICATED: {
					_loginButton.setText(getString(R.string.login_button_logged_in));
					break;
				}
				case DISCONNECTED: {
					Toast.makeText(_context, R.string.error_disconnected, Toast.LENGTH_SHORT).show();
					_loginButton.setText(getString(R.string.login_button));
					break;
				}
			}
		}

		@Override
		public void onError(XmppService.XmppError error) {
			switch (error) {
				case CONNECT_FAILURE: {
					Toast.makeText(_context, R.string.error_unable_to_connect, Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}

		@Override
		public void onPresence(Presence presence) {

		}
	};
}
