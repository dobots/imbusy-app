package nl.dobots.imbusy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jivesoftware.smack.packet.Presence;

import java.util.ArrayList;
import java.util.List;

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
 * Created on 0-0-15
 *
 * @author Bart van Vliet
 * Based on: login template
 */

public class LoginActivity extends Activity implements LoaderCallbacks<Cursor> {
	private static final String TAG = LoginActivity.class.getCanonicalName();
	private Context _context;

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
//	private UserLoginTask _loginTask = null;

	// UI references.
	private AutoCompleteTextView _phoneNumberView;
	private EditText _passwordView;
//	private View _progressView;
//	private View _progressViewText;
//	private View _loginFormView;
//	private View _loginProgressLayout;
	private ProgressDialog _progressDialog;

	private XmppService _xmppService = null;
	private ServiceConnection _xmppServiceConnection = new ServiceConnection() {
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		_context = this;

		if (_xmppService == null) {
			bindService(new Intent(this, XmppService.class), _xmppServiceConnection, Context.BIND_AUTO_CREATE);
		}

		setContentView(R.layout.activity_login);

		// Set up the login form.
		_phoneNumberView = (AutoCompleteTextView) findViewById(R.id.phoneNumber);
		populateAutoComplete();

		_passwordView = (EditText) findViewById(R.id.password);
		_passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		SharedPreferences preferences = getSharedPreferences(ImBusyApp.getInstance().getPreferencesFile(), MODE_PRIVATE);
		String number = preferences.getString(ImBusyApp.PREFERENCE_PHONE_NUMBER, null);
		String password = preferences.getString(ImBusyApp.PREFERENCE_PASSWORD, null);
		if (number != null) {
			_phoneNumberView.setText(number);
			if (password != null) {
				_passwordView.setText(password);
			}
		}

		Button signInButton = (Button) findViewById(R.id.sign_in_button);
		signInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});

//		_loginFormView = findViewById(R.id.login_form);
//		_progressViewText = findViewById(R.id.login_progress_text);
//		_progressView = findViewById(R.id.login_progress);
//		_loginProgressLayout = findViewById(R.id.login_progress_layout);
		_progressDialog = new ProgressDialog(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		_xmppService.removeListener(_xmppListener);
		unbindService(_xmppServiceConnection);
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
//		if (_loginTask != null) {
//			return;
//		}

		// Reset errors.
		_phoneNumberView.setError(null);
		_passwordView.setError(null);

		// Store values at the time of the login attempt.
		String phoneNumber = _phoneNumberView.getText().toString();
		String password = _passwordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password, if the user entered one.
		if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
			_passwordView.setError(getString(R.string.error_invalid_password));
			focusView = _passwordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(phoneNumber)) {
			_phoneNumberView.setError(getString(R.string.error_field_required));
			focusView = _phoneNumberView;
			cancel = true;
		} else if (!isPhoneNumberValid(phoneNumber)) {
			_phoneNumberView.setError(getString(R.string.error_invalid_email));
			focusView = _phoneNumberView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
//			// Show a progress spinner, and kick off a background task to
//			// perform the user login attempt.
//			showProgress(true);
//			_loginTask = new UserLoginTask(phoneNumber, password);
//			_loginTask.execute((Void) null);

			phoneNumber = phoneNumber.replaceAll(" ","");
			phoneNumber = phoneNumber.replaceAll("\\+", "00");
			SharedPreferences preferences = getSharedPreferences(ImBusyApp.getInstance().getPreferencesFile(), MODE_PRIVATE);
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString(ImBusyApp.PREFERENCE_PHONE_NUMBER, phoneNumber);
			editor.putString(ImBusyApp.PREFERENCE_PASSWORD, password);
			editor.commit();

			_xmppService.xmppLogin();

			// From here we use the xmpp listener to wait for the result
			showProgress(true);
		}
	}

	private boolean isPhoneNumberValid(String number) {
		return number.matches("\\+?[0-9 ]+");
	}

	private boolean isPasswordValid(String password) {
		return password.length() > 8;
	}

	final XmppServiceListener _xmppListener = new XmppServiceListener() {
		@Override
		public void onConnectStatus(XmppService.XmppStatus status) {
			switch (status) {
				case AUTHENTICATED: {
					// Successfully signed in
					showProgress(false);
					finish();
					break;
				}
			}
		}

		@Override
		public void onError(XmppService.XmppError error) {
			switch (error) {
				case CONNECT_FAILURE: {
					// Already done by main activity?
//					Toast.makeText(_context, R.string.error_unable_to_connect, Toast.LENGTH_SHORT).show();
					showProgress(false);
					break;
				}
				case AUTHORIZATION_FAILURE: {
					_passwordView.setError(getString(R.string.error_incorrect_password));
					_passwordView.requestFocus();
					showProgress(false);
					break;
				}
			}
		}
		@Override
		public void onFriend(XmppService.XmppFriendEvent event, XmppFriend friend) {
		}
	};

	public void showProgress(boolean show) {
		if (show) {
			_progressDialog.setTitle(getString(R.string.text_signing_in));
//			_progressDialog.setMessage("Wait while scanning...");
			_progressDialog.setCancelable(false);
			_progressDialog.show();
		}
		else {
			_progressDialog.dismiss();
		}
	}

	/////////////////////////
	// Auto complete stuff //
	/////////////////////////

	private void populateAutoComplete() {
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		return new CursorLoader(this,
				// Retrieve data rows for the device user's 'profile' contact.
				Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
				ProfileQuery.PROJECTION,

				// Select only phone nrs.
				ContactsContract.Contacts.Data.MIMETYPE + " = ?",
				new String[]{ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE},

				// Show primary phone nr first. Note that there won't be
				// a primary phone nr if the user hasn't specified one.
				ContactsContract.Contacts.Data.IS_PRIMARY + " DESC"
		);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		List<String> phoneNumbers = new ArrayList<String>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			phoneNumbers.add(cursor.getString(ProfileQuery.NUMBER));
			cursor.moveToNext();
		}

		addPhoneNumbersToAutoComplete(phoneNumbers);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {

	}

	private interface ProfileQuery {
		String[] PROJECTION = {
				ContactsContract.CommonDataKinds.Phone.NUMBER,
				ContactsContract.CommonDataKinds.Phone.IS_PRIMARY
		};
		int NUMBER = 0;
		int IS_PRIMARY = 1;
	}


	private void addPhoneNumbersToAutoComplete(List<String> phoneNumberCollection) {
		//Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
		ArrayAdapter<String> adapter =
				new ArrayAdapter<String>(LoginActivity.this,
						android.R.layout.simple_dropdown_item_1line, phoneNumberCollection);

		_phoneNumberView.setAdapter(adapter);
	}

//	/**
//	 * Represents an asynchronous login/registration task used to authenticate
//	 * the user.
//	 */
//	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
//
//		private final String _phoneNumber;
//		private final String _password;
//
//		UserLoginTask(String phoneNumber, String password) {
//			phoneNumber = phoneNumber.replaceAll(" ","");
//			_phoneNumber = phoneNumber.replaceAll("\\+", "00");
//			_password = password;
//		}
//
//		@Override
//		protected Boolean doInBackground(Void... params) {
//
//
//
//			SharedPreferences preferences = getSharedPreferences(ImBusyApp.getInstance().getPreferencesFile(), MODE_PRIVATE);
//			SharedPreferences.Editor editor = preferences.edit();
//			editor.putString(ImBusyApp.PREFERENCE_PHONE_NUMBER, _phoneNumber);
//			editor.putString(ImBusyApp.PREFERENCE_PASSWORD, _password);
//			editor.commit();
//
//			_xmppService.xmppLogin();
//
//			// Poll if xmpp is authenticated, give it some time
//			for (int i=0; i<20; ++i) {
//				try {
//					Thread.sleep(200);
//					if (ImBusyApp.getInstance().getXmppStatus() == XmppService.XmppStatus.AUTHENTICATED) {
//						return true;
//					}
//				} catch (InterruptedException e) {
//					return false;
//				}
//			}
//			return false;
//		}
//
//		@Override
//		protected void onPostExecute(final Boolean success) {
//			_loginTask = null;
//			showProgress(false);
//
//			if (success) {
//				finish();
//			} else {
//				switch (ImBusyApp.getInstance().getXmppStatus()) {
//					case DISCONNECTED:{
//						Toast.makeText(_context, R.string.error_unable_to_connect, Toast.LENGTH_SHORT).show();
////						finish();
//						break;
//					}
//					case CONNECTED:{
//						_passwordView.setError(getString(R.string.error_incorrect_password));
//						_passwordView.requestFocus();
//						break;
//					}
//				}
//			}
//		}
//
//		@Override
//		protected void onCancelled() {
//			_loginTask = null;
//			showProgress(false);
//		}
//	}
}

