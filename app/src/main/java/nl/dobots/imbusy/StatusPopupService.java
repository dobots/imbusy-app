package nl.dobots.imbusy;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;

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
 * Created on 27-7-15
 *
 * @author Bart van Vliet
 */

public class StatusPopupService extends Service {
	private static final String TAG = StatusPopupService.class.getCanonicalName();
	private WindowManager _windowManager;
	private ImageView _statusImg;

	@Override public IBinder onBind(Intent intent) {
		// Not used
		return null;
	}

	@Override public void onCreate() {
		super.onCreate();

		_windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		_statusImg = new ImageView(this);
		_statusImg.setImageResource(R.drawable.status_busy_144);

		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);

		params.gravity = Gravity.CENTER;
//		params.gravity = Gravity.TOP | Gravity.LEFT;
//		params.x = 0;
//		params.y = 100;

		_windowManager.addView(_statusImg, params);

		// After some time, remove the image by stopping this service
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				stopSelf();
			}
		},
		Config.POPUP_TIMEOUT);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//		return super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		if (_statusImg != null) _windowManager.removeView(_statusImg);
//		// Remove all callbacks and messages that were posted
//		_handler.removeCallbacksAndMessages(null);
	}
}
