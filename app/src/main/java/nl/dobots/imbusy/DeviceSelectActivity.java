package nl.dobots.imbusy;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import nl.dobots.bluenet.extended.structs.BleDevice;
import nl.dobots.bluenet.extended.structs.BleDeviceMap;


public class DeviceSelectActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
	protected static final String TAG = DeviceSelectActivity.class.getCanonicalName();
	protected static final int BACKGROUND_DEFAULT_COLOR = 0x00000000;
	protected static final int BACKGROUND_SELECTED_COLOR = 0x660000FF;

	private ListView _deviceListView;
	private DeviceListAdapter _deviceListAdapter;
	private StoredBleDeviceList _storedDeviceList;
	private BleDeviceMap _scannedDeviceList;
	private List<BleDevice> _scannedDeviceListCopy;

	private Handler _handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_select);

		_storedDeviceList = ImBusyApp.getInstance().getStoredDeviceList();
		_scannedDeviceList = ImBusyApp.getInstance().getBle().getDeviceMap();
		_scannedDeviceListCopy = new ArrayList<>(_scannedDeviceList.values());

		initListView();
		initButtons();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// TODO: Start endless scan, stop interval scan
	}

	@Override
	protected void onPause() {
		super.onPause();
		// TODO: Stop endless scan, start interval scan
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		// Remove all callbacks and messages that were posted
		_handler.removeCallbacksAndMessages(null);
	}

	private void initListView() {
		_deviceListView = (ListView) findViewById(R.id.deviceListView);
		_deviceListAdapter = new DeviceListAdapter();

		_deviceListView.setAdapter(_deviceListAdapter);
		// Activate the Click even of the List items
		_deviceListView.setOnItemClickListener(this);

		// Update the list view every second with newly scanned devices
		_handler.post(new Runnable() {
			@Override
			public void run() {
				updateListView();
				_handler.postDelayed(this, 1000);
			}
		});
	}

	private void initButtons(){
//		final Button doneButton = (Button) findViewById(R.id.doneButton);
//		doneButton.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				finish();
//			}
//		});
	}

	private void updateListView() {
		_scannedDeviceListCopy = new ArrayList<>(_scannedDeviceList.values());
		_deviceListAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		BleDevice device = _scannedDeviceListCopy.get(position);
		Log.d(TAG, "clicked item " + position + " " + device.getAddress());
		if (!_storedDeviceList.contains(device.getAddress())) {
			view.setBackgroundColor(BACKGROUND_SELECTED_COLOR);
			_storedDeviceList.add(new StoredBleDevice(device.getAddress(), device.getName()));
		}
		else {
			view.setBackgroundColor(BACKGROUND_DEFAULT_COLOR);
			_storedDeviceList.remove(device.getAddress());
		}
	}

	private class DeviceListAdapter extends BaseAdapter {

		public DeviceListAdapter() {
		}

		@Override
		public int getCount() {
			return _scannedDeviceListCopy.size();
		}

		@Override
		public Object getItem(int position) {
			return _scannedDeviceListCopy.get(position);
		}

		@Override
		public long getItemId(int position) {
			// Here we can give each item a certain ID, if we want.
			return 0;
		}

		private class ViewHolder {
			protected TextView deviceNameView;
			protected TextView deviceInfoView;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				//LayoutInflater layoutInflater = LayoutInflater.from(getContext());
				LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				convertView = layoutInflater.inflate(R.layout.device_item, null);

				// ViewHolder prevents calling findViewById too often,
				// now it only gets called on creation of a new convertView
				ViewHolder viewHolder = new ViewHolder();
				viewHolder.deviceNameView = (TextView)convertView.findViewById(R.id.deviceName);
				viewHolder.deviceInfoView = (TextView)convertView.findViewById(R.id.deviceInfo);
				convertView.setTag(viewHolder);
			}

			ViewHolder viewHolder = (ViewHolder)convertView.getTag();
			BleDevice device = (BleDevice)getItem(position);

			if (device != null) {
				viewHolder.deviceNameView.setText(device.getName());
				String infoText = getResources().getString(R.string.address_prefix) + " " + device.getAddress();
				infoText += "\n" + getResources().getString(R.string.rssi_prefix) + " " + device.getRssi();
				viewHolder.deviceInfoView.setText(infoText);
				if (_storedDeviceList.contains(device.getAddress())) {
					convertView.setBackgroundColor(BACKGROUND_SELECTED_COLOR);
				}
				else {
					convertView.setBackgroundColor(BACKGROUND_DEFAULT_COLOR);
				}
			}

			return convertView;
		}
	}
}
