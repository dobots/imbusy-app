package nl.dobots.imbusy;

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
 * Created on 19-8-15
 *
 * @author Bart van Vliet
 */

public class Config {
	public static final int IMBUSY_NOTIFICATION_ID_FRIEND_REQUEST = 1;

	// Background of selected and nonselected items in a listview
	public static final int BACKGROUND_DEFAULT_COLOR = 0x00000000;
	public static final int BACKGROUND_SELECTED_COLOR = 0x660000FF;

	// Settings for XMPP
	public static final String XMPP_DOMAIN = "dobots.customers.luna.net";
	public static final String XMPP_HOST = "dobots.customers.luna.net";
	public static final int XMPP_PORT = 5222;

	// Popup
	public static final int POPUP_TIMEOUT = 3000;

	// Time (ms) before busy changes to available if no stored device has been scanned
	public static final int BUSY_TIMEOUT = 30000;

	public static final int BLE_SCAN_INTERVAL = 1000; // 1 second scanning
	public static final int BLE_SCAN_PAUSE = 9000; // 9 seconds pause
}
