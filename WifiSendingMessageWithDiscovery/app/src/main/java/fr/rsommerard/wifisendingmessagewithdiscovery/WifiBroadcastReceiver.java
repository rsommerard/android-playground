package fr.rsommerard.wifisendingmessagewithdiscovery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;

public class WifiBroadcastReceiver extends BroadcastReceiver {

    private MainActivity mActivity;

    public WifiBroadcastReceiver(MainActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        // WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION == Wifi enabled or not.
        // WifiManager.SUPPLICANT_STATE_CHANGED_ACTION == Wifi enabled or not AND connected or not.

        if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
            mActivity.setWifiNetworkSupplicantState(
                    (SupplicantState) intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE));
        }
    }
}
