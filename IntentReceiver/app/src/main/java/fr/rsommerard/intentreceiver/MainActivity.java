package fr.rsommerard.intentreceiver;

import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "IntentReceiver";

    private static final String JEAN = "fr.rsommerard.intentsender.JEAN";
    private IntentFilter mIntentFilter;
    private JeanReceiver mJeanReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(JEAN);

        mJeanReceiver = new JeanReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mJeanReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mJeanReceiver, mIntentFilter);
    }


}
