package com.example.vivek;

import android.app.Application;
import android.content.Context;
import android.os.Process;
import androidx.multidex.MultiDex;
import androidx.appcompat.app.AppCompatDelegate;
import android.util.Log;

public class AllPDFApplication extends Application {
    public final String TAG = AllPDFApplication.class.getSimpleName();

    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        checkAppReplacingState();
    }

    public void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }

    private void checkAppReplacingState() {

        Log.d(this.TAG, "app start...");
        if (getResources() == null) {
            Log.d(this.TAG, "app is replacing...kill");
            Process.killProcess(Process.myPid());
        }
    }
}
