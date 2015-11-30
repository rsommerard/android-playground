package fr.rsommerard.wifidirectplayground;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "WDPMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startServiceButton = (Button) findViewById(R.id.button_start_service);
        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService();
            }
        });

        Button stopServiceButton = (Button) findViewById(R.id.button_stop_service);
        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService();
            }
        });
    }

    private void startService() {
        Log.d(TAG, "startService P2PService");
        startService(new Intent(this, P2PService.class));
    }

    private void stopService() {
        Log.d(TAG, "stopService P2PService");
        stopService(new Intent(this, P2PService.class));
    }
}
