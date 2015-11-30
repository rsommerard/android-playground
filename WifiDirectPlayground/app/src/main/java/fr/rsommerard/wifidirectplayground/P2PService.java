package fr.rsommerard.wifidirectplayground;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class P2PService extends Service {

    private final String TAG = "WDPP2PService";
    private final String SERVICE_TYPE = "_presence._tcp";
    private final String SERVICE_NAME = "_rsp2p";

    private IntentFilter mWifiIntentFilter;
    private BroadcastReceiver mWifiDirectBroadcastReceiver;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiP2pChannel;
    private WifiP2pDnsSdServiceInfo mWifiP2pDnsSdServiceInfo;
    private WifiP2pManager.DnsSdServiceResponseListener mDnsSdServiceResponseListener;
    private WifiP2pManager.DnsSdTxtRecordListener mDnsSdTxtRecordListener;
    private WifiP2pDnsSdServiceRequest mWifiP2pDnsSdServiceRequest;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        initIntentFilter();

        mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiP2pChannel = mWifiP2pManager.initialize(this, getMainLooper(), null);

        // Provider part
        startServiceRegistration();


        // User Part
        discoverService();


    }

    private void discoverService() {
        mDnsSdServiceResponseListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice device) {
                Log.d(TAG, "Service Found: " + device.deviceName + " - " + instanceName + " - " + registrationType);
            }
        };

        mDnsSdTxtRecordListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> record, final WifiP2pDevice device) {
                Log.d(TAG, "Device name: " + device.deviceName);
                Log.d(TAG, "status: " + record.get("status"));
            }
        };

        mWifiP2pManager.setDnsSdResponseListeners(mWifiP2pChannel, mDnsSdServiceResponseListener, mDnsSdTxtRecordListener);

        mWifiP2pDnsSdServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        mWifiP2pManager.addServiceRequest(mWifiP2pChannel, mWifiP2pDnsSdServiceRequest,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Service request added with success");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.e(TAG, "Add service request failed: " + getFaillureReasonName(reason));
                    }
                }
        );

        mWifiP2pManager.discoverServices(mWifiP2pChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Services discovery initiated");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Services discovery failed: " + getFaillureReasonName(reason));
            }
        });
    }

    private void startServiceRegistration() {
        Map<String, String> record = new HashMap<String, String>();
        record.put("status", "available");

        mWifiP2pDnsSdServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_NAME, SERVICE_TYPE, record);

        mWifiP2pManager.addLocalService(mWifiP2pChannel, mWifiP2pDnsSdServiceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Local service added with success");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Local add service failed: " + getFaillureReasonName(reason));
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mWifiDirectBroadcastReceiver);

        mWifiP2pManager.removeServiceRequest(mWifiP2pChannel, mWifiP2pDnsSdServiceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Service request removed with success");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Remove service request failed: " + getFaillureReasonName(reason));
            }
        });

        mWifiP2pManager.removeLocalService(mWifiP2pChannel, mWifiP2pDnsSdServiceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Local service removed with success");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Local remove service failed: " + getFaillureReasonName(reason));
            }
        });
    }

    private void initIntentFilter() {
        mWifiIntentFilter = new IntentFilter();
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION); // Useful for peer connection info
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION); // Not useful for service
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION); // Not useful for service
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION); // Useful for wifi enabled or not
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION); // Useful to get device name and IP address

        mWifiDirectBroadcastReceiver = new WifiDirectBroadcastReceiver();
        registerReceiver(mWifiDirectBroadcastReceiver, mWifiIntentFilter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String getFaillureReasonName(int reason) {
        switch(reason) {
            case WifiP2pManager.ERROR:
                return "ERROR";
            case WifiP2pManager.BUSY:
                return "BUSY";
            case WifiP2pManager.P2P_UNSUPPORTED:
                return "P2P_UNSUPPORTED";
            default:
                return "UNKNOWN";
        }
    }
}
