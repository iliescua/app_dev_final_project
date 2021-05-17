/*
 * MapsActivity
 * Author: Andrew Iliescu, Sam Jansen
 * Date: 5/15/21
 * This file launches the Google Maps Activity to display
 * the route the user traversed as well as provide easily
 * intractable data points
 */
package edu.msoe.flashboard;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.msoe.flashboard.databinding.ActivityMapsBinding;
import io.realm.Realm;
import io.realm.RealmResults;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private RealmResults<CoordData> results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get data from MainActivity
        Realm.init(getApplicationContext());
        Realm coordDB = Realm.getDefaultInstance();
        results = coordDB.where(CoordData.class).findAll();

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        int count = 1;

        //Move camera to users starting location and create line detailing route ran
        for (CoordData data : results) {
            LatLng current = new LatLng(data.getLatitude(), data.getLongitude());
            mMap.addMarker(new MarkerOptions().position(current).title("Point: " + count).snippet(data.toString()));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
            count++;
        }
    }
}