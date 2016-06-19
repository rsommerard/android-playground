package fr.rsommerard.gsmanalyzer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.security.Provider;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GSMAnalyzer";

    private final int PERMISSIONS = 42;

    private Button mAnalyzeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAnalyzeButton = (Button) findViewById(R.id.btn_analyze);
        assert mAnalyzeButton != null;
        mAnalyzeButton.setEnabled(false);
        mAnalyzeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                analyzeGSMNetwork();
            }
        });

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE},
                    PERMISSIONS);
        } else {
            mAnalyzeButton.setEnabled(true);
        }
    }

    private void analyzeGSMNetwork() {
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        String networkOperatorName = manager.getNetworkOperatorName();
        TextView networkOperatorNameTextView = (TextView) findViewById(R.id.txtvw_network_operator_name);
        networkOperatorNameTextView.setText(getString(R.string.network_operator_name) + " " + networkOperatorName);

        int networkType = manager.getNetworkType();
        String networkTypeName = getNetworkTypeName(networkType);
        TextView networkTypeNameTextView = (TextView) findViewById(R.id.txtvw_network_type_name);
        networkTypeNameTextView.setText(getString(R.string.network_type_name) + " " + networkTypeName);

        List<CellInfo> cells = manager.getAllCellInfo();
        for (CellInfo cell : cells) {
            if (!cell.isRegistered()) {
                continue;
            }

            if (cell instanceof CellInfoLte) {
                CellSignalStrengthLte lte = ((CellInfoLte) cell).getCellSignalStrength();

                String signalLevelName = getSignalLevelName(lte.getLevel());
                TextView signalLevelNameTextView = (TextView) findViewById(R.id.txtvw_signal_level_name);
                signalLevelNameTextView.setText(getString(R.string.signal_level_name) + " " + signalLevelName);

                String dbm = Integer.toString(lte.getDbm());
                TextView dbmTextView = (TextView) findViewById(R.id.txtvw_dbm);
                dbmTextView.setText(getString(R.string.dbm) + " " + dbm);

                String asu = Integer.toString(lte.getAsuLevel());
                TextView asuTextView = (TextView) findViewById(R.id.txtvw_asu);
                asuTextView.setText(getString(R.string.asu) + " " + asu);
            } else if (cell instanceof CellInfoCdma) {
                CellSignalStrengthCdma cdma = ((CellInfoCdma) cell).getCellSignalStrength();

                String signalLevelName = getSignalLevelName(cdma.getLevel());
                TextView signalLevelNameTextView = (TextView) findViewById(R.id.txtvw_signal_level_name);
                signalLevelNameTextView.setText(getString(R.string.signal_level_name) + " " + signalLevelName);

                String dbm = Integer.toString(cdma.getDbm());
                TextView dbmTextView = (TextView) findViewById(R.id.txtvw_dbm);
                dbmTextView.setText(getString(R.string.dbm) + " " + dbm);

                String asu = Integer.toString(cdma.getAsuLevel());
                TextView asuTextView = (TextView) findViewById(R.id.txtvw_asu);
                asuTextView.setText(getString(R.string.asu) + " " + asu);
            } else if (cell instanceof CellInfoGsm) {
                CellSignalStrengthGsm gsm = ((CellInfoGsm) cell).getCellSignalStrength();

                String signalLevelName = getSignalLevelName(gsm.getLevel());
                TextView signalLevelNameTextView = (TextView) findViewById(R.id.txtvw_signal_level_name);
                signalLevelNameTextView.setText(getString(R.string.signal_level_name) + " " + signalLevelName);

                String dbm = Integer.toString(gsm.getDbm());
                TextView dbmTextView = (TextView) findViewById(R.id.txtvw_dbm);
                dbmTextView.setText(getString(R.string.dbm) + " " + dbm);

                String asu = Integer.toString(gsm.getAsuLevel());
                TextView asuTextView = (TextView) findViewById(R.id.txtvw_asu);
                asuTextView.setText(getString(R.string.asu) + " " + asu);
            } else if (cell instanceof CellInfoWcdma) {
                CellSignalStrengthWcdma wcdma = ((CellInfoWcdma) cell).getCellSignalStrength();

                String signalLevelName = getSignalLevelName(wcdma.getLevel());
                TextView signalLevelNameTextView = (TextView) findViewById(R.id.txtvw_signal_level_name);
                signalLevelNameTextView.setText(getString(R.string.signal_level_name) + " " + signalLevelName);

                String dbm = Integer.toString(wcdma.getDbm());
                TextView dbmTextView = (TextView) findViewById(R.id.txtvw_dbm);
                dbmTextView.setText(getString(R.string.dbm) + " " + dbm);

                String asu = Integer.toString(wcdma.getAsuLevel());
                TextView asuTextView = (TextView) findViewById(R.id.txtvw_asu);
                asuTextView.setText(getString(R.string.asu) + " " + asu);
            }
        }

        String deviceId = manager.getDeviceId();

        String deviceSoftwareVersion = manager.getDeviceSoftwareVersion();

        int phoneType = manager.getPhoneType();
        String phoneTypeName = getPhoneTypeName(phoneType);

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestSingleUpdate(provider, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String latitude = Double.toString(location.getLatitude());
                String longitude = Double.toString(location.getLongitude());
                TextView locationTextView = (TextView) findViewById(R.id.txtvw_location);
                locationTextView.setText(getString(R.string.location) + " [lat: " + latitude + ", lon: " + longitude + "]");
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        }, getMainLooper());
    }

    private String getSignalLevelName(int signalLevel) {
        switch (signalLevel) {
            case CellSignalStrength.SIGNAL_STRENGTH_GREAT:
                return "GREAT";
            case CellSignalStrength.SIGNAL_STRENGTH_GOOD:
                return "GOOD";
            case CellSignalStrength.SIGNAL_STRENGTH_MODERATE:
                return "MODERATE";
            case CellSignalStrength.SIGNAL_STRENGTH_POOR:
                return "POOR";
            case CellSignalStrength.SIGNAL_STRENGTH_NONE_OR_UNKNOWN:
            default:
                return "NONE OR UNKNOWN";
        }
    }

    private String getPhoneTypeName(int phoneType) {
        switch (phoneType) {
            case TelephonyManager.PHONE_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.PHONE_TYPE_GSM:
                return "GSM";
            case TelephonyManager.PHONE_TYPE_SIP:
                return "SIP";
            case TelephonyManager.PHONE_TYPE_NONE:
                return "NONE";
            default:
                return "UNKNOWN";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }

                    mAnalyzeButton.setEnabled(true);
                }
            }
        }
    }

    private String getNetworkTypeName(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "1xRTT";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "EHRPD";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "EVDO_0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "EVDO_A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "EVDO_B";
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPAP";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "IDEN";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
            default:
                return "UNKNOWN";
        }
    }
}
