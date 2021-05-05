/*
 * MainActivity
 * Author: Andrew Iliescu, Sam Jansen
 * Date: 4/28/21
 * This file is the main Activity that greets the user upon launching the app
 * it has a simple UI that displays information for the user to use when
 * driving/racing. Also, reading sensor data and handling most events takes place
 * in this file
 */
package edu.msoe.flashboard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private Realm coordDB;
    private LocationCallback locationCallback;
    private static final float CONVERSION_FACTOR = 2.23694f;
    private TextView speedTB;
    private static final int PERMISSIONS_ALL = 1;
    private static final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET};
    private GMeter gMeter;
    private float[] accelXZ;

    /**
     * This method is run when the app is first launched and sets everything up
     *
     * @param savedInstanceState the current saved state of the app
     */
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        gMeter = findViewById(R.id.g_meter);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        speedTB = findViewById(R.id.speedTB);
        FusedLocationProviderClient flpc = LocationServices.getFusedLocationProviderClient(this);
        Realm.init(this);
        coordDB = Realm.getDefaultInstance();

        updateGMeter();

        //Check to ensure necessary permissions provided
        if (!hasPermissions()) {
            Toast.makeText(this, "Please allow permissions if you haven't already", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_ALL);
        }

        //Register accelerometer sensor
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_UI);

        //Register location requests
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10);
        locationRequest.setFastestInterval(1);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        startLocationUpdates();
        flpc.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    /**
     * This method is called whenever a sensor has new data and goes through and checks which sensor
     * changed and whether this chang warrants updating the step count
     *
     * @param event this variable gives access to the info on the sensor
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x_accel = event.values[0];
            float y_accel = event.values[1];
            float z_accel = event.values[2];
            //TODO Use accel data to update G-Meter
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * This helper method triggers the LocationCallback feature that is used to get the current location
     */
    private void startLocationUpdates() {
        locationCallback = new LocationCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                coordDB.beginTransaction();
                CoordData coordData = coordDB.createObject(CoordData.class);
                coordData.setLongitude(location.getLongitude());
                coordData.setLatitude(location.getLatitude());
                coordData.setAltitude(location.getAltitude());
                coordDB.commitTransaction();
                speedTB.setText(Integer.toString((int) (location.getSpeed() * CONVERSION_FACTOR)));
            }
        };
    }

    private void updateGMeter() {
        gMeter.updatePoint(1.0f, 1.0f);
    }

    /**
     * This method is called to create the overflow menu for the app
     *
     * @param menu reference to overflow menu
     * @return true for completion
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    /**
     * This method handles what happens when a MenuItem is pressed
     *
     * @param item the MenuItem pressed
     * @return true for completion
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //TODO
        return true;
    }

    /**
     * Helper method to check whether necessary permissions are provided
     *
     * @return boolean expression based on whether permissions are provided
     */
    private boolean hasPermissions() {
        boolean containsPermission = true;
        if (getApplicationContext() != null && PERMISSIONS != null) {
            for (String permission : PERMISSIONS) {
                containsPermission = ActivityCompat.checkSelfPermission(getApplicationContext(),
                        permission) == PackageManager.PERMISSION_GRANTED;
                if (!containsPermission) {
                    break;
                }
            }
        }
        return containsPermission;
    }
}