package com.ape.applockdemo;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLockedPackages(this.getApplicationContext());
    }

    private ArrayList<String> getLockedPackages(Context context) {
        Uri queryUri = Uri.parse("content://content://com.applock.tinno.provider.lockedapps/get");
        ArrayList<String> lockedPackages = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(queryUri, new String[0], "", new String[0], "");
        if (cursor == null) {
            Log.i("AppLock", "no locked packages cursor == null");
            return lockedPackages;
        }
        if (cursor.getCount() < 1) {
            Log.i("AppLock", "no locked packages");
            cursor.close();
            return lockedPackages;
        }
        while (cursor.moveToNext()) {
            String pkgName = cursor.getString(0);
            lockedPackages.add(pkgName);
        }

        Log.i("AppLock", "locked app = " + lockedPackages);
        return lockedPackages;
    }
}
