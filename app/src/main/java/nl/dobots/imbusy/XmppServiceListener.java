package nl.dobots.imbusy;

import org.jivesoftware.smack.packet.Presence;

/**
 * Created by Bart van Vliet on 12-8-15.
 */
public interface XmppServiceListener {
	void onConnectStatus(XmppService.XmppStatus status);
	void onError(XmppService.XmppError error);
	void onFriend(XmppService.XmppFriendEvent event, XmppFriend friend);
	// Etc..
}
