package fr.rsommerard.wifidirect;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements Handler.Callback {

    public static final String SERVICE_NAME = "_rsp2pdirect";

    // TODO: see if _presence or _http
    public static final String SERVICE_TYPE = "_presence._tcp";

    public static final String TAG = "wifidirect";

    private WifiDirectBroadcastReceiver mWifiDirectBroadcastReceiver;
    private ListView mMainListview;
    private ArrayList<String> mLoggerList;
    private ArrayAdapter<String> mLoggerAdapter;
    private IntentFilter mWifiIntentFilter;
    private WifiP2pManager mWifiP2pManager;
    private Channel mWifiP2pChannel;

    private Handler mHandler;
    private ServerSocket mServerSocket;

    private WifiP2pDevice mPeer;
    private int mPeerPort;

    private List<WifiP2pDevice> mWifiP2pPeers;
    private WifiP2pDnsSdServiceRequest mWifiP2pDnsSdServiceRequest;

    private final String mDeviceMode = "Client";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWifiDirectBroadcastReceiver = new WifiDirectBroadcastReceiver(this);

        mHandler = new Handler(this);

        mWifiP2pPeers = new ArrayList<WifiP2pDevice>();

        mLoggerList = new ArrayList<String>();
        mLoggerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                mLoggerList);

        mMainListview = (ListView) findViewById(R.id.main_listview);

        mMainListview.setAdapter(mLoggerAdapter);

        addAndNotify("Device mode: " + mDeviceMode);

        mWifiIntentFilter = new IntentFilter();
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mWifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiP2pChannel = mWifiP2pManager.initialize(this, getMainLooper(), null);

        mServerSocket = null;

        try {
            mServerSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, String> record = new HashMap<String, String>();
        record.put("port", String.valueOf(mServerSocket.getLocalPort()));

        WifiP2pDnsSdServiceInfo wifiP2pDnsSdServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_NAME, SERVICE_TYPE, record);

        mWifiP2pManager.addLocalService(mWifiP2pChannel, wifiP2pDnsSdServiceInfo, new ActionListener() {
            @Override
            public void onSuccess() {
                addAndNotify("Local service added: " + SERVICE_NAME);
                Log.d(TAG, "Local service added with success");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.e(TAG, "Local add service failed: " + reasonCode);
            }
        });

        if (mDeviceMode.equals("Client")) {
            mWifiP2pManager.setDnsSdResponseListeners(mWifiP2pChannel,
                    new DnsSdServiceResponseListener() {
                        @Override
                        public void onDnsSdServiceAvailable(String instanceName,
                                                            String registrationType,
                                                            WifiP2pDevice device) {
                            addAndNotify("Service found: " + instanceName);
                            Log.d(MainActivity.TAG, "Service Found: " + instanceName + " - " + registrationType);
                        }
                    },
                    new DnsSdTxtRecordListener() {
                        @Override
                        public void onDnsSdTxtRecordAvailable(String fullDomainName,
                                                              Map<String, String> record,
                                                              final WifiP2pDevice device) {
                            addAndNotify("Device name: " + device.deviceName);
                            Log.d(MainActivity.TAG, "Device name: " + device.deviceName);
                            Log.d(TAG, device.deviceName + ": port => " + record.get("port"));

                            mPeer = device;
                            mPeerPort = Integer.parseInt(record.get("port"));
                            Log.d(TAG, "Peer port: " + mPeerPort);

                            if (fullDomainName.contains(SERVICE_NAME)) {
                                WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
                                wifiP2pConfig.deviceAddress = device.deviceAddress;
                                wifiP2pConfig.wps.setup = WpsInfo.PBC;

                                if (mWifiP2pDnsSdServiceRequest != null)
                                    mWifiP2pManager.removeServiceRequest(mWifiP2pChannel,
                                            mWifiP2pDnsSdServiceRequest, new ActionListener() {
                                                @Override
                                                public void onSuccess() {
                                                    Log.d(MainActivity.TAG, "Service request removed");
                                                }

                                                @Override
                                                public void onFailure(int reason) {
                                                    Log.d(MainActivity.TAG, "Remove service request failed: " + reason);
                                                }
                                            }
                                    );


                                mWifiP2pManager.connect(mWifiP2pChannel, wifiP2pConfig, new ActionListener() {

                                    @Override
                                    public void onSuccess() {
                                        addAndNotify("Connecting to " + device.deviceName);
                                        Log.d(MainActivity.TAG, "Connecting to " + device.deviceName);
                                    }

                                    @Override
                                    public void onFailure(int reason) {
                                        Log.d(MainActivity.TAG, "Connection failed: " + reason);
                                    }
                                });
                            }
                        }
                    }
            );
        }

        mWifiP2pDnsSdServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        mWifiP2pManager.addServiceRequest(mWifiP2pChannel, mWifiP2pDnsSdServiceRequest,
                new ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Service request added with success");
                    }

                    @Override
                    public void onFailure(int error) {
                        Log.e(TAG, "Add service request failed: " + error);
                    }
                }
        );

        mWifiP2pManager.discoverServices(mWifiP2pChannel, new ActionListener() {
            @Override
            public void onSuccess() {
                //addAndNotify("Service discovery started");
                Log.d(TAG, "Services discovery initiated");
            }

            @Override
            public void onFailure(int code) {
                Log.e(TAG, "Services discovery failed: " + code);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mWifiDirectBroadcastReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mWifiP2pManager != null && mWifiP2pChannel != null) {
            mWifiP2pManager.removeGroup(mWifiP2pChannel, new ActionListener() {

                @Override
                public void onFailure(int reasonCode) {
                    Log.d(TAG, "Disconnect failed: " + reasonCode);
                }

                @Override
                public void onSuccess() {
                }

            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mWifiDirectBroadcastReceiver, mWifiIntentFilter);
    }

    public void addAndNotify(String element) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm:ss.SSS");
        Date date = new Date();
        mLoggerList.add("[" + timeFormat.format(date) + "]\n" + element);
        mLoggerAdapter.notifyDataSetChanged();
    }

    public void peersProcess(WifiP2pInfo wifiP2pInfo) {
        if (wifiP2pInfo.isGroupOwner) {
            addAndNotify("ServerThread");
            new ServerThread(mServerSocket, mHandler).start();
        } else {
            addAndNotify("ClientThread");
            new ClientThread(wifiP2pInfo.groupOwnerAddress, mPeerPort, mHandler).start();
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        addAndNotify(message.obj.toString());
        return true;
    }
}
