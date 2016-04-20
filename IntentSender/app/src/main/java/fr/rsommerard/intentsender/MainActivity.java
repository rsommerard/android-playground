package fr.rsommerard.intentsender;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String JEAN = "fr.rsommerard.intentsender.JEAN";

    public static final String TAG = "IntentSender";
    private ScheduledExecutorService mExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mExecutor = Executors.newSingleThreadScheduledExecutor();
        mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                sendIntent();
            }
        }, 0, 7000, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mExecutor.shutdown();
    }

    private void sendIntent() {
        Log.d(TAG, "sendIntent()");

        Intent intent = new Intent(JEAN);
        sendBroadcast(intent);
    }
}
