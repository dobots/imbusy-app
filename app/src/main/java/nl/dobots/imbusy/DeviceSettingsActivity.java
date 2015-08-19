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
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;


public class DeviceSettingsActivity extends AppCompatActivity
		implements AdapterView.OnItemClickListener {
	private static final String TAG = DeviceSelectActivity.class.getCanonicalName();
	private static final int BACKGROUND_DEFAULT_COLOR = 0x00000000;
	private static final int BACKGROUND_SELECTED_COLOR = 0x660000FF;
	private static final int THRESHOLD_SLIDER_MIN = -100;


	private ListView _deviceListView;
//	private TextView _deviceNameView;
//	private TextView _deviceInfoView;
	private DeviceListAdapter _deviceListAdapter;
	private StoredBleDeviceList _deviceList;
	private List<StoredBleDevice> _deviceListCopy;
	private Handler _handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_settings);

		_deviceList = ImBusyApp.getInstance().getStoredDeviceList();
		_deviceListCopy = _deviceList.toList();
		_handler = new Handler();

		initListView();
		initButtons();
	}

	@Override
	protected void onPause() {
		super.onPause();
		ImBusyApp.getInstance().getStoredDeviceList().save();
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
//		_deviceListAdapter = new DeviceListAdapter(this, R.layout.device_item ,ImBusyApp.getInstance().getStoredDeviceList());
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
//		final Button doneButton = (Button) findViewById(R.id.doneButton);
//		doneButton.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				finish();
//			}
//		});
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

		private class ViewHolder {
			protected TextView deviceNameView;
			protected TextView deviceInfoView;
//			protected TextView thresholdView;
			protected SeekBar thresholdSlider;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d(TAG, "getView convertView=" + convertView + " position=" + position);
			if (convertView == null) {
				// LayoutInflater class is used to instantiate layout XML file into its corresponding View objects.
				LayoutInflater layoutInflater = (LayoutInflater) parent.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
				convertView = layoutInflater.inflate(R.layout.device_settings_item, null);

//				final ViewHolder viewHolder = new ViewHolder();
//				viewHolder.deviceNameView = (TextView) convertView.findViewById(R.id.deviceName);
//				viewHolder.deviceInfoView = (TextView) convertView.findViewById(R.id.deviceInfo);
////				viewHolder.thresholdView = (TextView) convertView.findViewById(R.id.thresholdText);
//				viewHolder.thresholdSlider = (SeekBar) convertView.findViewById(R.id.thresholdSlider);
//				convertView.setTag(viewHolder);
			}

//			ViewHolder viewHolder = (ViewHolder) convertView.getTag();

			StoredBleDevice device = (StoredBleDevice)getItem(position);

//			if (device != null) {
				TextView deviceNameView = (TextView)convertView.findViewById(R.id.deviceName);
				TextView deviceInfoView = (TextView)convertView.findViewById(R.id.deviceInfo);
				TextView thresholdView = (TextView)convertView.findViewById(R.id.thresholdText);
				SeekBar thresholdSlider = (SeekBar)convertView.findViewById(R.id.thresholdSlider);
				deviceNameView.setText(device.getName());
				deviceInfoView.setText(device.getAddress());
//				viewHolder.deviceNameView.setText(device.getName());
//				viewHolder.deviceInfoView.setText(device.getAddress());

				float threshold = device.getRssiThreshold();
//				thresholdView.setText(Float.toString(device.getRssiThreshold())); // annoying format
//				thresholdView.setText(String.format("%.2f", device.getRssiThreshold())); // trailing zeros
//				thresholdView.setText(new DecimalFormat("#.#").format(threshold)); // Nice
				thresholdView.setText(getResources().getString(R.string.threshold_prefix) + " " + Integer.toString((int) (threshold)));
				thresholdSlider.setMax(-THRESHOLD_SLIDER_MIN);
				thresholdSlider.setProgress((int) (device.getRssiThreshold() - THRESHOLD_SLIDER_MIN));
//				setOnSeekBarChangeListener(thresholdSlider, position, thresholdView);
				thresholdSlider.setTag(position);
				thresholdSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//				viewHolder.thresholdSlider.setMax(-THRESHOLD_SLIDER_MIN);
//				viewHolder.thresholdSlider.setProgress((int) (device.getRssiThreshold() - THRESHOLD_SLIDER_MIN));
//				viewHolder.thresholdSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						int position = (Integer)seekBar.getTag();
						Log.d(TAG, "position=" + position + " seekBar=" + seekBar);
						_deviceListCopy.get(position).setRssiThreshold((float) (seekBar.getProgress() + THRESHOLD_SLIDER_MIN));
						View parentView = (View) seekBar.getParent();
						if (parentView != null) {
							TextView thresholdView =(TextView)parentView.findViewById(R.id.thresholdText);
							thresholdView.setText(getResources().getString(R.string.threshold_prefix) + " " + Integer.toString((int) (_deviceListCopy.get(position).getRssiThreshold())));
						}
					}
				});


//				if (_deviceList.contains(device.getAddress())) {
//					convertView.setBackgroundColor(BACKGROUND_SELECTED_COLOR);
//				}
//				else {
//					convertView.setBackgroundColor(BACKGROUND_DEFAULT_COLOR);
//				}
//			}

			return convertView;
		}
	}
}
