package fr.rsommerard.maps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        LatLng myPosition;
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);

        Location location = locationManager.getLastKnownLocation(provider);

        if(location!=null) {
            // Getting latitude of the current location
            double latitude = location.getLatitude();

            // Getting longitude of the current location
            double longitude = location.getLongitude();

            // Creating a LatLng object for the current location
            LatLng lille1Center = new LatLng(50.60928, 3.140738);
            LatLng m5Center = new LatLng(50.609736, 3.13672);
            LatLng sullyCenter  = new LatLng(50.605589, 3.136591);
            LatLng pariselleCenter  = new LatLng(50.608367, 3.147342);
            LatLng baroisCenter  = new LatLng(50.612044, 3.143147);

            myPosition = new LatLng(latitude, longitude);

            mMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(15));

            mMap.addMarker(new MarkerOptions().position(m5Center).title("M5"));
            mMap.addMarker(new MarkerOptions().position(sullyCenter).title("Sully"));
            mMap.addMarker(new MarkerOptions().position(pariselleCenter).title("Pariselle"));
            mMap.addMarker(new MarkerOptions().position(baroisCenter).title("Barois"));
            mMap.addCircle(new CircleOptions().center(lille1Center).radius(700).fillColor(Color.argb(50, 200, 0, 0)).visible(true));
        }
    }
}
