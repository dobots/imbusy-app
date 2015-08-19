package nl.dobots.imbusy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
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
//		final Button doneButton = (Button) findViewById(R.id.doneButton);
//		doneButton.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				finish();
//			}
//		});

		final Button addButton = (Button) findViewById(R.id.addButton);
		addButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				addContact();
			}
		});
	}

	private void addContact() {
		// See http://developer.android.com/training/basics/intents/result.html
		Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
		startActivityForResult(intent, PICK_CONTACT);
	}

	private void addContact(String phoneNumber, String name) {
		phoneNumber = phoneNumber.replaceAll(" ", "");
		phoneNumber = phoneNumber.replaceAll("\\+", "00");

		Log.d(TAG, "Picked: number=" + phoneNumber + " name=" + name);
//		_contactList.add(new PhoneContact(phoneNumber, name));
//		_contactListCopy = _contactList.toList();
		_xmppService.xmppAddFriend(ImBusyApp.getXmppUsername(phoneNumber), name);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == PICK_CONTACT) {
			if (resultCode == RESULT_OK) {
				Uri uri = intent.getData();
				String[] projection = { ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME };
				Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
				if (cursor == null || !cursor.moveToFirst()) {
					return;
				}
				String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
				addContact(phoneNumber, name);
			}
		}
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final XmppFriend friend = _friendListCopy.get(position);
		Log.d(TAG, "clicked item " + position + " " + friend.getUsername());
		String number = ImBusyApp.getNumber(friend.getJid());
		String name = friend.getNick();

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle("Remove friend");
		dialogBuilder.setMessage("Do you want to remove " + name + " (" + number + ")?");
		dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				_xmppService.xmppRemoveFriend(friend.getJid());
			}
		});
		dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
//		dialogBuilder.setIcon();
		Dialog dialog = dialogBuilder.show();
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

				String infoText = "";
				switch (friend.getSubscriptionType()) {
					case from:
						infoText = "This person can see your status.\nAdd to see their status.";
						break;
					case none:
					case remove:
						infoText = "You are not (yet) allowed to see this persons status.\nRemove and add to re-request permission.";
						break;
					case both:
					case to:
						infoText = ImBusyApp.getInstance().getStatusText(ImBusyApp.getStatus(friend.getMode()));
						break;
				}
				contactInfoView.setText(friend.getUsername() + "\n" + infoText);
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
					break;
				}
				case REMOVED:{
					_friendListCopy = _friendList.toList();
					_contactListAdapter.notifyDataSetChanged();
					break;
				}
				case FRIEND_UPDATE:{
					_friendListCopy = _friendList.toList();
					_contactListAdapter.notifyDataSetChanged();
					break;
				}
				case FRIEND_REQUEST:{
					break;
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
