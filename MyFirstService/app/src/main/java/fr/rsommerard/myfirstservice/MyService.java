package fr.rsommerard.myfirstservice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class MyService extends Service {

    public static final String TAG = "MyService";

    private AlarmManager mAlarmManager;
    private PendingIntent mPendingIntent;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 60000;

        Log.d(TAG, "Set repeating");
        // setInexactRepeating save battery power
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, mPendingIntent);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "onDestroy");
        if (mAlarmManager != null) {
            mAlarmManager.cancel(mPendingIntent);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
