package nl.dobots.imbusy;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {
	private static final String TAG = MainActivity.class.getCanonicalName();
	private static final int STATUS_POLL_DELAY = 500;
	private Handler _handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		updateStatus();

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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
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

	public void setStatus(ImBusyApp.Status status) {
		if (status == null) {
			return;
		}
		String text = getResources().getString(R.string.status_prefix);
		switch (status) {
			case AVAILABLE:
				text += getResources().getString(R.string.status_available);
				break;
			case BUSY:
				text += getResources().getString(R.string.status_busy);
				break;
		}
		final TextView _statusTextView = (TextView) findViewById(R.id.status_text);
		_statusTextView.setText(text);
	}

	private void updateStatus() {
		if (ImBusyApp.getInstance() != null) {
			setStatus(ImBusyApp.getInstance().getStatus());
		}
	}
}
