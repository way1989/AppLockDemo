package com.ape.applockdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.cmcm.applock.service.CMAppLockDataService;

/**
 * for CM AppLock
 * 
 * @author liweiping
 * 
 * @hide
 */
public class AppLockService {
	private static final String TAG = "AppLockService";
	private static final String APP_LOCK_URL = "content://com.applock.tinno.provider.lockedapps/query/";
	private static AppLockService sAppLockService;
	private CMAppLockDataService mAppLockService;

	private AppLockService() {
	}

	public static AppLockService getInstance() {
		if (sAppLockService == null)
			sAppLockService = new AppLockService();
		return sAppLockService;
	}

	/**
	 * query from content provider
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	public boolean isAppLocked(Context context, String packageName) {
		try {
			String uri = APP_LOCK_URL + packageName;
			Cursor cursor = context.getContentResolver().query(Uri.parse(uri),
					null, null, null, null);
			if (cursor == null)
				return false;

			if (cursor.getCount() != 1) {
				cursor.close();
				return false;
			}

			cursor.moveToFirst();
			int status = cursor.getInt(0);
			cursor.close();
			android.util.Log.i(TAG, " isAppLocked packageName = " + packageName
					+ ",  status = " + status);
			return status == 1;
		} catch (Exception e) {
		}
		return false;
	}

	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mAppLockService = null;
			Log.i(TAG, "onServiceDisconnected... mAppLockService = "
					+ mAppLockService);
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mAppLockService = CMAppLockDataService.Stub.asInterface(service);
			Log.i(TAG, "onServiceConnected... mAppLockService = "
					+ mAppLockService);

		}
	};

	/**
	 * bind service
	 */
	public void bindApplockService(Context context) {
		if (mAppLockService != null)
			return;
		Intent bindIntent = new Intent();
		bindIntent.setPackage("com.applock.tinno");
		bindIntent.setAction("com.cmcm.applock.action.BIND_APPLOCK_SERVICE");
		context.bindService(bindIntent, mServiceConnection,
				Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
	}

	/**
	 * unbind service
	 * 
	 * @param context
	 */
	public void unbindApplockService(Context context) {
		if (mAppLockService == null)
			return;
		try {
			context.unbindService(mServiceConnection);
		} catch (Exception e) {

		}
	}
	/**
	 * get the AppLock service
	 * @return
	 */
	public CMAppLockDataService getService() {
		return mAppLockService;
	}

	/**
	 * query from AppLockService
	 * 
	 * @param packageName
	 * @return
	 */
	public boolean isAppLocked(String packageName) {
		if (mAppLockService == null)
			return false;
		try {
			boolean isAppLocked = mAppLockService.needLockTheApp(packageName);
			Log.d(TAG, "isAppLocked packageName = " + packageName
					+ ", mAppLockService = " + mAppLockService
					+ ", isAppLocked = " + isAppLocked);
			return isAppLocked;
		} catch (RemoteException e) {
			e.printStackTrace();
			Log.d(TAG, "isAppLocked packageName = " + packageName + ", e = "
					+ e);
		}
		return false;
	}
}
