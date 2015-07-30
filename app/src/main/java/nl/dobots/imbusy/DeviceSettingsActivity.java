package nl.dobots.imbusy;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;


public class DeviceSettingsActivity extends Activity implements AdapterView.OnItemClickListener {
	protected static final String TAG = DeviceSelectActivity.class.getCanonicalName();
	protected static final int BACKGROUND_DEFAULT_COLOR = 0x00000000;
	protected static final int BACKGROUND_SELECTED_COLOR = 0x660000FF;

	private ListView _deviceListView;
//	private TextView _deviceNameView;
//	private TextView _deviceInfoView;
	private DeviceListAdapter _deviceListAdapter;
	private StoredBleDeviceList _deviceList;
	private List<StoredBleDevice> _deviceListCopy;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_settings);

		_deviceList = ImBusyApp.getInstance().getBleDeviceList();
		_deviceListCopy = _deviceList.toList();

		initListView();
		initButtons();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
//		// Remove all callbacks and messages that were posted
//		_handler.removeCallbacksAndMessages(null);
	}

	private void initListView() {
		_deviceListView = (ListView) findViewById(R.id.deviceListView);
//		_deviceListAdapter = new DeviceListAdapter(this, R.layout.device_item ,ImBusyApp.getInstance().getBleDeviceList());
		_deviceListAdapter = new DeviceListAdapter();
		Log.d(TAG, "device list:");
		for (StoredBleDevice dev : _deviceListCopy) {
			Log.d(TAG, dev.getAddress());
		}
		_deviceListView.setAdapter(_deviceListAdapter);
		// Activate the Click even of the List items
		_deviceListView.setOnItemClickListener(this);
	}

	private void initButtons(){
		final Button doneButton = (Button) findViewById(R.id.doneButton);
		doneButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		StoredBleDevice device = _deviceListCopy.get(position);
		Log.d(TAG, "clicked item " + position + " " + device.getAddress());
//		if (!_deviceList.contains(device.getAddress())) {
//			view.setBackgroundColor(BACKGROUND_SELECTED_COLOR);
////			_deviceList.add(device);
//		}
//		else {
//			view.setBackgroundColor(BACKGROUND_DEFAULT_COLOR);
////			_deviceList.remove(device);
//		}
	}

	private class DeviceListAdapter extends BaseAdapter {

		public DeviceListAdapter() {
		}

		@Override
		public int getCount() {
			return _deviceListCopy.size();
		}

		@Override
		public Object getItem(int position) {
			return _deviceListCopy.get(position);
		}

		@Override
		public long getItemId(int position) {
			// Here we can give each item a certain ID, if we want.
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				//LayoutInflater layoutInflater = LayoutInflater.from(getContext());
				LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				convertView = layoutInflater.inflate(R.layout.device_settings_item, null);
			}

			StoredBleDevice device = (StoredBleDevice)getItem(position);
			Log.d(TAG, "device view:");
			Log.d(TAG, device.getAddress());


			if (device != null) {
				TextView deviceNameView = (TextView)convertView.findViewById(R.id.deviceName);
				TextView deviceInfoView = (TextView)convertView.findViewById(R.id.deviceInfo);
				deviceNameView.setText(device.getName());
				deviceInfoView.setText(device.getAddress());
//				if (_deviceList.contains(device.getAddress())) {
//					convertView.setBackgroundColor(BACKGROUND_SELECTED_COLOR);
//				}
//				else {
//					convertView.setBackgroundColor(BACKGROUND_DEFAULT_COLOR);
//				}
			}

			return convertView;
		}
	}
}
