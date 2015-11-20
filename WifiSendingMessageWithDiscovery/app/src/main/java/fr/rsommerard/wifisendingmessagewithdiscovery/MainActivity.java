package fr.rsommerard.wifisendingmessagewithdiscovery;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.ResolveListener;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager.RegistrationListener;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Handler.Callback {

    public static final String TAG = "wifimessage";

    public static final String SERVICE_NAME = "_RSP2P";
    public static final String SERVICE_TYPE = "_http._tcp.";

    private String mDeviceMode = "Client";
    private String mServiceName;

    private SupplicantState mWifiSupplicantState;
    private WifiBroadcastReceiver mWifiBroadcastReceiver;

    private List<String> mLoggerList;
    private ArrayAdapter<String> mLoggerAdapter;
    private IntentFilter mWifiIntentFilter;
    private String mConnectionMode;
    private WifiManager mWifiManager;
    private ListView mMainListview;
    private ConnectivityManager mConnectivityManager;

    private NsdManager mNsdManager;

    private Handler mHandler;
    private RegistrationListener mRegistrationListener;
    private DiscoveryListener mDiscoveryListener;
    private ResolveListener mResolveListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler(this);

        mWifiBroadcastReceiver = new WifiBroadcastReceiver(this);

        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

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

        if (mNsdManager != null) {
            mNsdManager.unregisterService(mRegistrationListener);
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mWifiBroadcastReceiver, mWifiIntentFilter);

        if (mConnectionMode.equals("WIFI") && mWifiSupplicantState.equals(SupplicantState.COMPLETED)) {
            printDeviceIP();
            if (mDeviceMode.equals("Server")) {
                ServerSocket serverSocket = null;

                try {
                    serverSocket = new ServerSocket(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                NsdServiceInfo serviceInfo  = new NsdServiceInfo();

                // TODO: Set serviceName with deviceName + SERVICE_NAME

                serviceInfo.setServiceName(mDeviceMode + MainActivity.SERVICE_NAME);
                serviceInfo.setServiceType(MainActivity.SERVICE_TYPE);
                serviceInfo.setPort(serverSocket.getLocalPort());

                mRegistrationListener = new RegistrationListener() {
                    @Override
                    public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                        Log.d(TAG, "Service registration failed: " + errorCode);
                    }

                    @Override
                    public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                        Log.d(TAG, "Service unregistration failed: " + errorCode);
                    }

                    @Override
                    public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                        mServiceName = serviceInfo.getServiceName();
                        Log.d(TAG, "Service registered as: " + mServiceName);
                    }

                    @Override
                    public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                        Log.d(TAG, "Service unregistered: " + serviceInfo.getServiceName());
                    }
                };

                mNsdManager.registerService(
                        serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);

                new ServerThread(serverSocket, mHandler).start();
            } else {
                mResolveListener = new ResolveListener() {
                    @Override
                    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                        // Called when the resolve fails.  Use the error code to debug.
                        Log.d(TAG, "Resolve failed: " + errorCode);
                    }

                    @Override
                    public void onServiceResolved(NsdServiceInfo serviceInfo) {
                        Log.d(TAG, "Resolve Succeeded: \n" + serviceInfo);

                        // TODO: Check if thois statement is necessary
                        if (serviceInfo.getServiceName().equals(mServiceName)) {
                            Log.d(TAG, "Same IP");
                            return;
                        }

                        int servicePort = serviceInfo.getPort();
                        InetAddress serviceHost = serviceInfo.getHost();

                        new ClientThread(serviceHost, servicePort, mHandler).start();
                    }
                };

                mDiscoveryListener = new DiscoveryListener() {
                    @Override
                    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                        Log.d(TAG, "Discovery failed: " + errorCode);
                    }

                    @Override
                    public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                        Log.d(TAG, "Discovery failed: " + errorCode);
                    }

                    @Override
                    public void onDiscoveryStarted(String serviceType) {
                        Log.d(TAG, "Service discovery started");
                    }

                    @Override
                    public void onDiscoveryStopped(String serviceType) {
                        Log.d(TAG, "Discovery stopped: " + serviceType);
                    }

                    @Override
                    public void onServiceFound(NsdServiceInfo serviceInfo) {
                        Log.d(TAG, "Service discovery success: " + serviceInfo);

                        if (!serviceInfo.getServiceType().equals(SERVICE_TYPE)) {
                            // Service type is the string containing the protocol and
                            // transport layer for this service.
                            Log.d(TAG, "Unknown Service Type: " + serviceInfo.getServiceType());
                        } else if (serviceInfo.getServiceName().equals(mServiceName)) {
                            // The name of the service tells the user what they'd be
                            // connecting to. It could be "Bob's Chat App".
                            Log.d(TAG, "Same machine: " + mServiceName);
                        } else if (serviceInfo.getServiceName().contains(SERVICE_NAME)){
                            mNsdManager.resolveService(serviceInfo, mResolveListener);
                        }
                    }

                    @Override
                    public void onServiceLost(NsdServiceInfo serviceInfo) {
                        Log.d(TAG, "Service lost: " + serviceInfo);
                    }
                };

                mNsdManager.discoverServices(
                        SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
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
