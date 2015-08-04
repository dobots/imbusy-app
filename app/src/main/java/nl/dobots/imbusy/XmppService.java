package nl.dobots.imbusy;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.preference.Preference;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntries;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.TLSUtils;

import java.util.Collection;
import java.util.Random;

/**
 * Created by Bart van Vliet on 4-8-15.
 */
public class XmppService extends Service {
	private static final String TAG = XmppService.class.getCanonicalName();
	private Handler _handler = new Handler();
	private AbstractXMPPConnection _connection;
	private Presence _presence;
	private Roster _roster;


	@Override
	public void onCreate() {
		super.onCreate();
		XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
		configBuilder.setUsernameAndPassword("name", "password");
		configBuilder.setServiceName("domain");
//		configBuilder.setHost("host");
		configBuilder.setPort(1234);
		configBuilder.setDebuggerEnabled(true);

		// TODO: make sure this is secure
//		configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);

		// Need this to accept self signed certificate
		// TODO: use these examples:
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

//		XMPPTCPConnectionConfiguration config = configBuilder.build();
		_connection = new XMPPTCPConnection(configBuilder.build());

		Log.d(TAG, "Roster:");
		_roster = Roster.getInstanceFor(_connection);
		Collection<RosterEntry> rosterEntries = _roster.getEntries();
		for (RosterEntry rosterEntry : rosterEntries) {
			Log.d(TAG, rosterEntry.toString());
		}
		Log.d(TAG, "- End of roster -");

		_roster.addRosterListener(new RosterListener() {
			@Override
			public void entriesAdded(Collection<String> addresses) {

			}

			@Override
			public void entriesUpdated(Collection<String> addresses) {

			}

			@Override
			public void entriesDeleted(Collection<String> addresses) {

			}

			@Override
			public void presenceChanged(Presence presence) {
				Log.i(TAG, "Presence changed: from=" + presence.getFrom() + " status=" + presence.getStatus() + " mode=" + presence.getMode());
				Log.d(TAG, presence.toString());
			}
		});

		_roster.setRosterLoadedAtLogin(true);

		// TODO: manual buddy acceptance
		// a PacketListener should be registered that listens for Presence packets that have a type of Presence.Type.subscribe
//		_roster.setSubscriptionMode(Roster.SubscriptionMode.manual);
		_roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);


		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					_connection.connect();
					_connection.login();
				} catch (Exception e) {
					Log.e(TAG, "Failed to connect");
					e.printStackTrace();
					return;
				}
				_presence = new Presence(Presence.Type.available);
//				_presence.setMode(Presence.Mode.available);
				_presence.setMode(Presence.Mode.away);
				_presence.setStatus("Some status text");

				try {
					_connection.sendStanza(_presence);
				} catch (SmackException.NotConnectedException e) {
					Log.e(TAG, "Not connected");
					e.printStackTrace();
				}

//				try {
//					_roster.createEntry("name@host.com", "nick", null);
//				} catch (Exception e) {
//					Log.e(TAG, "Failed to add entry");
//					e.printStackTrace();
//					return;
//				}

			}
		}).start();




	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		_connection.disconnect();
	}
}
