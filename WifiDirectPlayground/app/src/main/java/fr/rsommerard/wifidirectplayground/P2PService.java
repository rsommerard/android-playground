package fr.rsommerard.wifidirectplayground;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class P2PService extends Service {

    private static final String SERVICE_NAME = "_rsp2p";
    private static final String SERVICE_TYPE = "_presence._tcp";

    private final String TAG = "P2PService";

    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiP2pChannel;
    private WifiP2pDnsSdServiceInfo mWifiP2pDnsSdServiceInfo;
    private WifiP2pDnsSdServiceRequest mWifiP2pDnsSdServiceRequest;
    private IntentFilter mWifiIntentFilter;
    private WifiDirectBroadcastReceiver mWifiDirectBroadcastReceiver;
    private WifiP2pDevice mOwnDevice;
    private Timer mTimerTask;
    private Timer mPrint;
    private Timer mConnect;
    private Timer mDisconnect;
    private ConnectToFirstDevice mConnectToFirstDevice;
    private DisconnectFromFirstDevice mDisconnectFromFirstDevice;
    private DiscoverServicesTimerTask mDiscoverServicesTimerTask;
    private PrintDevicesTimerTask mPrintDevicesTimerTask;

    private List<Device> devices;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        devices = new ArrayList<>();

        mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiP2pChannel = mWifiP2pManager.initialize(this, getMainLooper(), null);

        Map<String, String> record = new HashMap<>();
        record.put("port", "42");

        mWifiP2pDnsSdServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_NAME, SERVICE_TYPE, record);

        mWifiP2pManager.addLocalService(mWifiP2pChannel, mWifiP2pDnsSdServiceInfo, new AddLocalServiceActionListener());

        mWifiP2pManager.setDnsSdResponseListeners(mWifiP2pChannel,
                new SetDnsSdServiceResponseListener(),
                new SetDnsSdTxtRecordListener()
        );

        mWifiP2pDnsSdServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        mWifiP2pManager.addServiceRequest(mWifiP2pChannel, mWifiP2pDnsSdServiceRequest,
                new AddServiceRequestActionListener()
        );

        mWifiIntentFilter = new IntentFilter();
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);     // OK
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);          // OK
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);    // OK

        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

        mWifiDirectBroadcastReceiver = new WifiDirectBroadcastReceiver();
        registerReceiver(mWifiDirectBroadcastReceiver, mWifiIntentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mWifiDirectBroadcastReceiver);

        clearDirectDiscovery();

    }

    private void enabledDirectDiscovery() {
        mTimerTask = new Timer();
        mDiscoverServicesTimerTask = new DiscoverServicesTimerTask();
        mTimerTask.scheduleAtFixedRate(mDiscoverServicesTimerTask, 0, 10000);

        mPrint = new Timer();
        mPrintDevicesTimerTask = new PrintDevicesTimerTask();
        mPrint.scheduleAtFixedRate(mPrintDevicesTimerTask, 5000, 42000);

        mConnect = new Timer();
        mConnectToFirstDevice = new ConnectToFirstDevice();
        mConnect.scheduleAtFixedRate(mConnectToFirstDevice, 20000, 42000);

        mDisconnect = new Timer();
        mDisconnectFromFirstDevice = new DisconnectFromFirstDevice();
        mDisconnect.scheduleAtFixedRate(mDisconnectFromFirstDevice, 30000, 42000);
    }

    private void disabledDirectDiscovery() {
        clearDirectDiscovery();
    }

    private void clearDirectDiscovery() {
        mTimerTask.cancel();
        mPrint.cancel();
        mWifiP2pManager.clearLocalServices(mWifiP2pChannel, new ClearLocalServicesActionListener());
        mWifiP2pManager.clearServiceRequests(mWifiP2pChannel, new ClearServiceRequestsActionListener());
        mWifiP2pManager.cancelConnect(mWifiP2pChannel, new CancelConnectActionListener());

        mWifiP2pManager.removeGroup(mWifiP2pChannel, new RemoveGroupActionListener());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class DiscoverServicesTimerTask extends TimerTask {
        @Override
        public void run() {
            devices.clear();
            mWifiP2pManager.discoverServices(mWifiP2pChannel, new DiscoverServicesActionListener());
        }
    }

    private class PrintDevicesTimerTask extends TimerTask {
        @Override
        public void run() {
            if (devices.isEmpty()) {
                Log.d(TAG, "[]");
                return;
            }

            for (Device device : devices) {
                Log.d(TAG, "[" + device.address + ", " + device.port + "]");
            }
        }
    }

    private class ConnectToFirstDevice extends TimerTask {

        @Override
        public void run() {
            if (devices.isEmpty()) {
                return;
            }

            WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
            wifiP2pConfig.deviceAddress = devices.get(0).address;
            wifiP2pConfig.wps.setup = WpsInfo.PBC;

            mWifiP2pManager.connect(mWifiP2pChannel, wifiP2pConfig, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Connecting to " + devices.get(0).address);
                }

                @Override
                public void onFailure(int reason) {
                    Log.d(TAG, "Connection failed: " + getReasonName(reason));
                }
            });
        }
    }

    private class DisconnectFromFirstDevice extends TimerTask {

        @Override
        public void run() {
            mWifiP2pManager.cancelConnect(mWifiP2pChannel, new CancelConnectActionListener());
        }
    }

    private class WifiDirectBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, action);

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int wifiState = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,
                        WifiP2pManager.WIFI_P2P_STATE_DISABLED);

                if (wifiState == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Log.d(TAG, "Wifi P2P enabled");
                    enabledDirectDiscovery();
                } else {
                    Log.d(TAG, "Wifi P2P disabled");
                    disabledDirectDiscovery();
                }
            }

            if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                WifiP2pInfo wifiP2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
                Log.d(TAG, wifiP2pInfo.toString());

                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                Log.d(TAG, networkInfo.toString());

                if (networkInfo.isConnected()) {
                    // we are connected with the other device, request connection
                    // info to find group owner IP
                    Log.d(TAG, "Devices connected");
                    //disabledDirectDiscovery();
                } else {
                    // It's a disconnect
                    Log.d(TAG, "Devices disconnected");
                    //enabledDirectDiscovery();
                }
            }

            if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                mOwnDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                Log.d(TAG, "Own device name: " + mOwnDevice.deviceName);
            }

            if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
                // TODO: check if discovery is running.

            }
        }
    }

    private class AddLocalServiceActionListener implements WifiP2pManager.ActionListener {

        @Override
        public void onSuccess() {
            Log.d(TAG, "Local service added with success");
        }

        @Override
        public void onFailure(int reason) {
            Log.e(TAG, "Local add service failed: " + getReasonName(reason));
        }
    }

    private class SetDnsSdServiceResponseListener implements
            WifiP2pManager.DnsSdServiceResponseListener {

        @Override
        public void onDnsSdServiceAvailable(String instanceName,
                                            String registrationType,
                                            WifiP2pDevice srcDevice) {
            Log.d(TAG, "Service found: " + instanceName + " - " + registrationType);
        }
    }

    private class SetDnsSdTxtRecordListener implements WifiP2pManager.DnsSdTxtRecordListener {

        @Override
        public void onDnsSdTxtRecordAvailable(String fullDomainName,
                                              Map<String, String> txtRecordMap,
                                              final WifiP2pDevice srcDevice) {
            Log.d(TAG, "Device found: " + srcDevice.deviceName);
            Log.d(TAG, "Device found address: " + srcDevice.deviceAddress);

            Device newDevice = new Device(srcDevice.deviceAddress, txtRecordMap.get("port"));

            if (!devices.contains(newDevice)) {
                devices.add(newDevice);
            }

            /*if (fullDomainName.contains(SERVICE_NAME)) {

            }*/
        }
    }

    private class AddServiceRequestActionListener implements WifiP2pManager.ActionListener {

        @Override
        public void onSuccess() {
            Log.d(TAG, "Service request added with success");
        }

        @Override
        public void onFailure(int reason) {
            Log.e(TAG, "Add service request failed: " + getReasonName(reason));
        }
    }

    private class DiscoverServicesActionListener implements WifiP2pManager.ActionListener {

        @Override
        public void onSuccess() {
            Log.d(TAG, "Services discovery initiated");
        }

        @Override
        public void onFailure(int reason) {
            Log.e(TAG, "Services discovery failed: " + getReasonName(reason));
        }
    }

    private class ClearLocalServicesActionListener implements WifiP2pManager.ActionListener {

        @Override
        public void onSuccess() {
            Log.d(TAG, "Local services cleared");
        }

        @Override
        public void onFailure(int reason) {
            Log.e(TAG, "Cleaning local services failed: " + getReasonName(reason));
        }
    }

    private class ClearServiceRequestsActionListener implements WifiP2pManager.ActionListener {
        @Override
        public void onSuccess() {
            Log.d(TAG, "Local service requests cleared");
        }

        @Override
        public void onFailure(int reason) {
            Log.e(TAG, "Cleaning local service requests failed: " + getReasonName(reason));
        }
    }

    private class CancelConnectActionListener implements WifiP2pManager.ActionListener {
        @Override
        public void onSuccess() {
            Log.d(TAG, "Connect canceled");
        }

        @Override
        public void onFailure(int reason) {
            Log.e(TAG, "Cleaning local service requests failed: " + getReasonName(reason));
        }
    }

    private String getReasonName(int reason) {
        switch(reason) {
            case WifiP2pManager.P2P_UNSUPPORTED:
                return "P2P_UNSUPPORTED";
            case WifiP2pManager.BUSY:
                return "BUSY";
            case WifiP2pManager.ERROR:
                return "ERROR";
            default:
                return "UKNOWN_REASON";
        }
    }

    private class RemoveGroupActionListener implements WifiP2pManager.ActionListener {
        @Override
        public void onSuccess() {
            Log.d(TAG, "Remove group success");
        }

        @Override
        public void onFailure(int reason) {
            Log.e(TAG, "Removing group failed: " + getReasonName(reason));
        }
    }
}
