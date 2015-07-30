package nl.dobots.imbusy;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by Bart van Vliet on 27-7-15.
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
		_statusImg.setImageResource(R.drawable.status_busy);

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
		3000); // TODO: magic nr
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//		return super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (_statusImg != null) _windowManager.removeView(_statusImg);
//		// Remove all callbacks and messages that were posted
//		_handler.removeCallbacksAndMessages(null);
	}
}
