package fr.rsommerard.serviceplayground;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class ServiceTwo extends Service {

    private final String TAG = "SPServiceTwo";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "ServiceTwo started");

        for(int i = 0; i <= 1000000000; i++) {
            if (i % 200000000 == 0) {
                Log.d(TAG, "ServiceTwo: " + String.valueOf(i));
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ServiceTwo stopped");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}