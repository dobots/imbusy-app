package nl.dobots.imbusy;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
	private static final String TAG = ContactsActivity.class.getCanonicalName();
	private static final int PICK_CONTACT = 1;

	private ListView _contactListView;
	private ContactListAdapter _contactListAdapter;
//	private PhoneContactList _contactList;
//	private List<PhoneContact> _contactListCopy;
	private XmppFriendList _friendList;
	private List<XmppFriend> _friendListCopy;


	private XmppService _xmppService = null;
	private ServiceConnection _xmppServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			_xmppService = ((XmppService.XmppBinder)service).getService();
			_xmppService.addListener(_xmppListener);
			_friendList = _xmppService.getFriendList();
			_friendListCopy = _friendList.toList();
			_contactListAdapter.notifyDataSetChanged();
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			_xmppService = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);
//		_contactList = ImBusyApp.getInstance().getContactList();
//		_contactListCopy = _contactList.toList();
		_friendListCopy = new ArrayList<>(0);
		bindService(new Intent(this, XmppService.class), _xmppServiceConnection, Context.BIND_AUTO_CREATE);

		initListView();
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
//		// Remove all callbacks and messages that were posted
//		_handler.removeCallbacksAndMessages(null);
	}

	private void initListView() {
		_contactListView = (ListView) findViewById(R.id.contactListView);
		_contactListAdapter = new ContactListAdapter();
		Log.d(TAG, "contact list:");
//		for (PhoneContact contact : _contactListCopy) {
//			Log.d(TAG, contact.getNumber() + " " + contact.getName());
//		}
		for (XmppFriend friend : _friendListCopy) {
			Log.d(TAG, friend.getUsername() + " " + friend.getNick());
		}
		_contactListView.setAdapter(_contactListAdapter);
		// Activate the Click even of the List items
		_contactListView.setOnItemClickListener(this);
	}

	private void initButtons(){
		final Button doneButton = (Button) findViewById(R.id.doneButton);
		doneButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		final Button addButton = (Button) findViewById(R.id.addButton);
		addButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				addContact();
			}
		});
	}

	private void addContact() {
		Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(intent, PICK_CONTACT);
	}

	private void addContact(String phoneNumber, String name) {
		phoneNumber = phoneNumber.replaceAll(" ", "");
		phoneNumber = phoneNumber.replaceAll("\\+", "00");

		Log.d(TAG, "Picked: number=" + phoneNumber + " name=" + name);
//		_contactList.add(new PhoneContact(phoneNumber, name));
//		_contactListCopy = _contactList.toList();
		_xmppService.xmppAddFriend(phoneNumber, name);
	}




	//TODO: Uses deprecated function
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == PICK_CONTACT) {
			if (resultCode == RESULT_OK) {
				Uri uri = intent.getData();
				Cursor cursor =  managedQuery(uri, null, null, null, null);
				if (!cursor.moveToFirst()) {
					return;
				}
				String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
				String hasPhone = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER));
				if (!hasPhone.equals("1")) {
					return;
				}

				String[] projection = { ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME };
				Cursor phones = getContentResolver().query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						projection,
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
						null,
						null);
				phones.moveToFirst();
				String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
				addContact(phoneNumber, name);
			}
		}
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//		PhoneContact contact = _contactListCopy.get(position);
//		Log.d(TAG, "clicked item " + position + " " + contact.getNumber());
//		_xmppService.xmppRemoveFriend(contact.getNumber());
		XmppFriend friend = _friendListCopy.get(position);
		Log.d(TAG, "clicked item " + position + " " + friend.getUsername());
		_xmppService.xmppRemoveFriend(friend.getUsername());
	}

	private class ContactListAdapter extends BaseAdapter {

		public ContactListAdapter() {
		}

		@Override
		public int getCount() {
//			return _contactListCopy.size();
			return _friendListCopy.size();
		}

		@Override
		public Object getItem(int position) {
//			return _contactListCopy.get(position);
			return _friendListCopy.get(position);
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
				convertView = layoutInflater.inflate(R.layout.contact_item, null);
			}

//			final PhoneContact contact = (PhoneContact)getItem(position);
			final XmppFriend friend = (XmppFriend)getItem(position);

//			if (contact != null) {
			if (friend != null) {
				TextView contactNameView = (TextView)convertView.findViewById(R.id.contactName);
				TextView contactInfoView = (TextView)convertView.findViewById(R.id.contactInfo);

//				contactNameView.setText(contact.getName());
//				contactInfoView.setText(contact.getNumber());
				contactNameView.setText(friend.getNick());
				contactInfoView.setText(friend.getUsername());
			}

			return convertView;
		}
	}


	private final XmppServiceListener _xmppListener = new XmppServiceListener() {
		@Override
		public void onConnectStatus(XmppService.XmppStatus status) {
		}

		@Override
		public void onError(XmppService.XmppError error) {
		}

		@Override
		public void onFriend(XmppService.XmppFriendEvent event, XmppFriend friend) {
			switch (event) {
				case ADDED:{
					_friendListCopy = _friendList.toList();
					_contactListAdapter.notifyDataSetChanged();
				}
				case REMOVED:{
					_friendListCopy = _friendList.toList();
					_contactListAdapter.notifyDataSetChanged();
				}
				case PRESENCE_UPDATE:{

				}
				case FRIEND_REQUEST:{

				}
			}
		}
	};

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.menu_contacts, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//
//		//noinspection SimplifiableIfStatement
//		if (id == R.id.action_settings) {
//			return true;
//		}
//
//		return super.onOptionsItemSelected(item);
//	}
}
