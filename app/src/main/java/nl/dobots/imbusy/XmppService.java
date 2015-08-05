package nl.dobots.imbusy;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.TLSUtils;

/**
 * Created by Bart van Vliet on 4-8-15.
 */
public class XmppService extends Service {
	private static final String TAG = XmppService.class.getCanonicalName();
	private static final int STATUS_POLL_DELAY = 500;
	private XmppThread _xmppThread = null;
	private Handler _handler;

	/** Target we give to the xmpp runnable to send messages to, handled by MessageHandler. */
	private final Messenger _messengerIn = new Messenger(new MessageHandler());
	private Messenger _messengerOut;


	/** Handler of incoming messages from xmpp runnable */
	private class MessageHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case XmppThread.MSG_REGISTER: {
					Log.i(TAG, "Register xmpp runnable messenger");
					_messengerOut = msg.replyTo;
					onXmppInitialized();
					break;
				}
				case XmppThread.MSG_CONNECTED: {
					onXmppConnected();
					break;
				}
				default: {
					super.handleMessage(msg);
				}
			}
		}
	}

	private void sendMessage(android.os.Message msg) {
		if (_messengerOut == null || msg == null) {
			Log.e(TAG, "sendMessage() - messenger or msg is null");
			return;
		}
		try {
			_messengerOut.send(msg);
		} catch (RemoteException e) {
			Log.e(TAG, "sendMessage() exception:");
			e.printStackTrace();
		}
	}

	private void sendMessage(int msgType) {
		Message msg = Message.obtain(null, msgType);
		sendMessage(msg);
	}


	@Override
	public void onCreate() {
		super.onCreate();
		XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
		configBuilder.setUsernameAndPassword("name", "password");
		configBuilder.setServiceName("domain");
//		configBuilder.setHost("host");
		configBuilder.setPort(1234);
		configBuilder.setDebuggerEnabled(true);

		// TODO: make sure we use a secure connection!
//		configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);

		// Need this to accept self signed certificate
		// TODO: use these examples:
		// https://community.igniterealtime.org/thread/56000
		// http://www.codeproject.com/Articles/826045/Android-security-Implementation-of-Self-signed-SSL
		// http://randomizedsort.blogspot.nl/2010/09/step-to-step-guide-to-programming.html
		// https://blog.synyx.de/2010/06/android-and-self-signed-ssl-certificates/
		try {
			TLSUtils.acceptAllCertificates(configBuilder);
		} catch (Exception e) {
			Log.e(TAG, "Could not configure TLS");
			e.printStackTrace();
			return;
		}
		TLSUtils.disableHostnameVerificationForTlsCertificicates(configBuilder);



		// Set a resource name
		String resource = "android_";
		resource += android.os.Build.MODEL.replaceAll(" ", "_");
//		int postfix = new Random().nextInt(999) + 1; // number from 1 to 999, seeded by current time
//		resource += "_" + postfix;
		configBuilder.setResource(resource);

		XMPPTCPConnectionConfiguration config = configBuilder.build();
		_xmppThread = new XmppThread(config, _messengerIn);


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

	private void onXmppInitialized() {
		sendMessage(XmppThread.MSG_CONNECT);
	}

	private void onXmppConnected() {
		sendMessage(XmppThread.MSG_GET_FRIENDS);
	}



	private void updateStatus() {
		if (ImBusyApp.getInstance() == null) {
			return;
		}

		ImBusyApp.Status status = ImBusyApp.getInstance().getStatus();
		if (status == null) {
			return;
		}
		String xmppModeText;
		switch (status) {
			case AVAILABLE:
				xmppModeText = "available";
				break;
			case BUSY:
				xmppModeText = "dnd";
				break;
			default:
				xmppModeText = "available";
		}

		Message msg = Message.obtain(null, XmppThread.MSG_SET_STATUS);
		Bundle bundle = new Bundle();
//		bundle.putString("status", "");
		bundle.putString("mode", xmppModeText);
		msg.setData(bundle);
		sendMessage(msg);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		sendMessage(XmppThread.MSG_DISCONNECT);
//		if (_xmppThread != null) {
//			_xmppThread.stop();
//		}
	}
}
