package com.ape.applockdemo;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cmcm.applock.service.CMAppLockDataService;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private CMAppLockDataService mAppLockService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAppLockService = null;
            Log.i(TAG, "onServiceDisconnected... mAppLockService = " + mAppLockService);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAppLockService = CMAppLockDataService.Stub
                    .asInterface(service);
            Log.i(TAG, "onServiceConnected... mAppLockService = " + mAppLockService);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //bindApplockService(this);

        Button button = (Button) findViewById(R.id.test_btn);
        if (button != null)
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //boolean result = isAppLocked(getPackageName());
                    //Log.d(TAG, "onCreate() returned: " + result);
                    long start = System.currentTimeMillis();
                    boolean result = needShowLocker("com.android.mms");
                    long end = System.currentTimeMillis();
                    Toast.makeText(MainActivity.this, "isAppLocked = " + result + ", cost = " + (end - start) + "ms", Toast.LENGTH_SHORT).show();
                }
            });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //getLockedPackages(this.getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unbindApplockService(this);
    }

    private ArrayList<String> getLockedPackages(Context context) {
        Uri queryUri = Uri.parse("content://com.applock.tinno.provider.lockedapps/get");
        ArrayList<String> lockedPackages = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(queryUri, new String[0], "", new String[0], "");
        if (cursor == null) {
            Log.i(TAG, "no locked packages cursor == null");
            return lockedPackages;
        }
        if (cursor.getCount() < 1) {
            Log.i(TAG, "no locked packages");
            cursor.close();
            return lockedPackages;
        }
        while (cursor.moveToNext()) {
            String pkgName = cursor.getString(0);
            lockedPackages.add(pkgName);
        }

        Log.i(TAG, "locked app = " + lockedPackages);
        return lockedPackages;
    }

    public void bindApplockService(Context context) {
        Intent bindIntent = new Intent();
        bindIntent.setPackage("com.applock.tinno");
        bindIntent.setAction("com.cmcm.applock.action.BIND_APPLOCK_SERVICE");
        context.bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
    }

    public void unbindApplockService(Context context) {
        try {
            context.unbindService(mServiceConnection);
        } catch (Exception e) {

        }
    }

    public boolean isAppLocked(String packageName) {
        Log.d(TAG, "isAppLocked packageName = " + packageName
                + ", mAppLockService = " + mAppLockService);
        if (mAppLockService == null)
            return false;
        try {
            return mAppLockService.needLockTheApp(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.d(TAG, "isAppLocked packageName = " + packageName
                    + ", e = " + e);
        }
        return false;
    }

    private boolean needShowLocker(String pkgName) {
        String uri = "content://com.applock.tinno.provider.lockedapps/query/" + pkgName;
        Cursor cursor = getContentResolver().query(Uri.parse(uri), null, null, null, null);
        if (cursor == null)
            return false;

        if (cursor.getCount() != 1) {
            cursor.close();
            return false;
        }

        cursor.moveToFirst();
        int status = cursor.getInt(0);
        cursor.close();
        return status == 1;
    }

}
