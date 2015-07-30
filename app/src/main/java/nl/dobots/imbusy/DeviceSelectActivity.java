package nl.dobots.imbusy;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogRecord;

import nl.dobots.bluenet.extended.structs.BleDevice;
import nl.dobots.bluenet.extended.structs.BleDeviceMap;


public class DeviceSelectActivity extends Activity implements AdapterView.OnItemClickListener {
	protected static final String TAG = DeviceSelectActivity.class.getCanonicalName();
	protected static final int BACKGROUND_DEFAULT_COLOR = 0x00000000;
	protected static final int BACKGROUND_SELECTED_COLOR = 0x660000FF;

	private ListView _deviceListView;
//	private TextView _deviceNameView;
//	private TextView _deviceInfoView;
	private DeviceListAdapter _deviceListAdapter;
	private StoredBleDeviceList _deviceList;
	private BleDeviceMap _scannedDeviceList;
	private List<BleDevice> _scannedDeviceListCopy;

	private Handler _handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_select);

		_deviceList = ImBusyApp.getInstance().getBleDeviceList();
		_scannedDeviceList = ImBusyApp.getInstance().getBle().getDeviceMap();
		_scannedDeviceListCopy = new ArrayList<>(_scannedDeviceList.values());

		_deviceListView = (ListView) findViewById(R.id.deviceListView);
//		_deviceListAdapter = new DeviceListAdapter(this, R.layout.device_item ,ImBusyApp.getInstance().getBleDeviceList());
		_deviceListAdapter = new DeviceListAdapter();

		_deviceListView.setAdapter(_deviceListAdapter);
		// Activate the Click even of the List items
		_deviceListView.setOnItemClickListener(this);

		_handler.post(new Runnable() {
			@Override
			public void run() {
				updateList();
				_handler.postDelayed(this, 1000);
			}
		});
	}


	private void updateList() {
		_scannedDeviceListCopy = new ArrayList<>(_scannedDeviceList.values());
		_deviceListAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		BleDevice device = _scannedDeviceListCopy.get(position);
		Log.d(TAG, "clicked item " + position + " " + device.getAddress());
		if (!_deviceList.contains(device.getAddress())) {
			view.setBackgroundColor(BACKGROUND_SELECTED_COLOR);
			_deviceList.add(new StoredBleDevice(device.getAddress(), device.getName()));
		}
		else {
			view.setBackgroundColor(BACKGROUND_DEFAULT_COLOR);
			_deviceList.remove(device.getAddress());
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

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				//LayoutInflater layoutInflater = LayoutInflater.from(getContext());
				LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				convertView = layoutInflater.inflate(R.layout.device_item, null);
			}

			BleDevice device = (BleDevice)getItem(position);
			Log.d(TAG, "device view:");
			Log.d(TAG, device.getAddress());


			if (device != null) {
				TextView deviceNameView = (TextView)convertView.findViewById(R.id.deviceName);
				TextView deviceInfoView = (TextView)convertView.findViewById(R.id.deviceInfo);
				deviceNameView.setText(device.getName());
				deviceInfoView.setText(device.getAddress());
				if (_deviceList.contains(device.getAddress())) {
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
