package fr.rsommerard.wifisendingmessage;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Handler.Callback {

    public static final String TAG = "wifimessage";

    private String mDeviceMode = "Client";

    private SupplicantState mWifiSupplicantState;
    private WifiBroadcastReceiver mWifiBroadcastReceiver;

    private List<String> mLoggerList;
    private ArrayAdapter<String> mLoggerAdapter;
    private IntentFilter mWifiIntentFilter;
    private String mConnectionMode;
    private WifiManager mWifiManager;
    private ListView mMainListview;
    private ConnectivityManager mConnectivityManager;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler(this);

        mWifiBroadcastReceiver = new WifiBroadcastReceiver(this);

        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        mWifiSupplicantState = mWifiManager.getConnectionInfo().getSupplicantState();

        mLoggerList = new ArrayList<String>();
        mLoggerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                mLoggerList);

        mMainListview = (ListView) findViewById(R.id.main_listview);

        mMainListview.setAdapter(mLoggerAdapter);

        mConnectionMode = getConnectionMode();

        addAndNotify("Connection mode: " + mConnectionMode);
        addAndNotify("Device mode: " + mDeviceMode);

        mWifiIntentFilter = new IntentFilter();
        mWifiIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
    }

    public void setWifiNetworkSupplicantState(SupplicantState wifiSupplicantState) {
        mWifiSupplicantState = wifiSupplicantState;
        addAndNotify("Supplicant state: " + mWifiSupplicantState.toString());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mWifiBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mWifiBroadcastReceiver, mWifiIntentFilter);

        if (mConnectionMode.equals("WIFI") && mWifiSupplicantState.equals(SupplicantState.COMPLETED)) {
            printDeviceIP();
            if (mDeviceMode.equals("Server")) {
                new ServerThread(mHandler).start();
            } else {
                new ClientThread(mHandler).start();
            }
        }
    }

    public void printDeviceIP() {
        int ipAddress = mWifiManager.getConnectionInfo().getIpAddress();
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
        addAndNotify("Device IP: " + ip);
    }

    public void addAndNotify(String element) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm:ss.SSS");
        Date date = new Date();
        mLoggerList.add("[" + timeFormat.format(date) + "]\n" + element);
        mLoggerAdapter.notifyDataSetChanged();
    }

    private String getConnectionMode() {
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();

        if (networkInfo != null) { // connected to the internet
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                return "WIFI";
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                return "MOBILE";
            }
        }

        return "NONE";
    }

    @Override
    public boolean handleMessage(Message message) {
        addAndNotify(message.obj.toString());
        return true;
    }
}
