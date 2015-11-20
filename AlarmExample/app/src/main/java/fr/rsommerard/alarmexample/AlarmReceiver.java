package fr.rsommerard.alarmexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


public class AlarmReceiver extends BroadcastReceiver {

    public static final String TAG = "alarmexample";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "I'm running");
        Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();
    }
}
