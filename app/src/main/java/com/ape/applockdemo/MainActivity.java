package com.ape.applockdemo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cmcm.applock.service.CMAppLockDataService;

import java.io.File;
import java.io.FileOutputStream;
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
        //注册广播
        registerReceiver(mHomeKeyEventReceiver, new IntentFilter(
                Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        Button button = (Button) findViewById(R.id.test_btn);
        if (button != null)
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //boolean result = isAppLocked(getPackageName());
                    //Log.d(TAG, "onCreate() returned: " + result);
                    //boolean result = needShowLocker("com.android.mms");
//                    long start = System.currentTimeMillis();
//                    boolean result = isAppLocked("com.android.mms");
//                    long end = System.currentTimeMillis();
//                    Toast.makeText(MainActivity.this, "isAppLocked = " + result + ", cost = " + (end - start) + "ms", Toast.LENGTH_SHORT).show();
                    //new PictureTask().execute();
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
        //unbindApplockService(this);
        unregisterReceiver(mHomeKeyEventReceiver);
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
        cursor.close();
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
    class PictureTask extends AsyncTask<Void,Void,Boolean>{

        @Override
        protected Boolean doInBackground(Void... params) {
            Bitmap resultBitmap = getLockBitmap();
            if(resultBitmap == null || resultBitmap.isRecycled())
                return false;
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(new File(getFilesDir(), "lock.png"));
                resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                resultBitmap.recycle();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Toast.makeText(MainActivity.this, "result = " + aBoolean, Toast.LENGTH_SHORT).show();
        }
    }
    private Bitmap getLockBitmap() {
        final Bitmap lock = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_lock_black);
        if (lock == null || lock.isRecycled())
            return null;

        final int size = 600;
        int left = 0;
        int top = 0;
        if (size > lock.getWidth())
            left = (size - lock.getWidth()) / 2;
        if (size > lock.getHeight())
            top = (size - lock.getHeight()) / 2;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(0xFFF6F6F6);
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        canvas.drawBitmap(lock, left, top, paint);
        lock.recycle();
        return bitmap;
    }


    /**
     * 监听是否点击了home键将客户端推到后台
     */
    private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
        String SYSTEM_REASON = "reason";
        String SYSTEM_HOME_KEY = "homekey";
        String SYSTEM_HOME_KEY_LONG = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {
                    //表示按了home键,程序到了后台
                    Toast.makeText(getApplicationContext(), "home click", Toast.LENGTH_SHORT).show();
                }else if(TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG)){
                    //表示长按home键,显示最近使用的程序列表
                    Toast.makeText(getApplicationContext(), "home long click", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

}
