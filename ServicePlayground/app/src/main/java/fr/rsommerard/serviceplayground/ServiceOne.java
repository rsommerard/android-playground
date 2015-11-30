package fr.rsommerard.serviceplayground;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class ServiceOne extends Service {
    private final String TAG = "SPServiceOne";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "ServiceOne started");
        startService(new Intent(this, ServiceTwo.class));
        startService(new Intent(this, ServiceThree.class));
        for(int i = 0; i <= 1000000000; i++) {
            if (i % 200000000 == 0) {
                Log.d(TAG, "ServiceOne: " + String.valueOf(i));
            }
        }

        Log.d(TAG, "ServiceOne after startService");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ServiceOne stopped");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
