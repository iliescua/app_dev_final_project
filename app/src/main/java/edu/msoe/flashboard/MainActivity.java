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
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.view.View;
import android.widget.Switch;
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
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private Realm coordDB;
    private LocationCallback locationCallback;
    private static final float CONVERSION_FACTOR = 2.23694f;
    private TextView speedTB;
    private static final int PERMISSIONS_ALL = 1;
    private GMeter gMeter;
    private float[] accelXZ;
    private double[] lastCords;
    private long myCurrentTimeMillis;
    private boolean isLogging = false;
    private static final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

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
        Switch simpleSwitch = (Switch) findViewById(R.id.switch_logging);
        speedTB = findViewById(R.id.speedTB);
        FusedLocationProviderClient flpc = LocationServices.getFusedLocationProviderClient(this);
        Realm.init(this);
        coordDB = Realm.getDefaultInstance();
        accelXZ = new float[4];
        lastCords = new double[4];

        //Check to ensure necessary permissions provided
        if (!hasPermissions()) {
            Toast.makeText(this, "Please allow permissions if you haven't already", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_ALL);
        }

        //Register accelerometer sensor
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_UI);

        //Register location requests
        LocationRequest locationRequest = LocationRequest.create();

        // 0 = literally as fast as it can
        locationRequest.setInterval(0);

        // We don't really need this, in fact we want as steady as possible
        //locationRequest.setFastestInterval(1);

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        startLocationUpdates();
        flpc.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());



        //Listener for Logging Data switch/toggle
        findViewById(R.id.switch_logging).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Save log to file if logging enabled -> logging disabled
                if (isLogging == true){
                    //Save current log data to file
                    try {
                        saveDBToFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //Set logging flag to false
                    isLogging = simpleSwitch.isChecked();
                } else{ // Enable logging if disabled -> enabled
                    //Set logging flag to true
                    isLogging = simpleSwitch.isChecked();
                }


            }
        });
    }

    /**
     * This method is called whenever a sensor has new data and goes through and checks which sensor
     * changed and whether this chang warrants updating the step count
     *
     * @param event this variable gives access to the info on the sensor
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            accelXZ[0] = event.values[0];
            accelXZ[1] = event.values[1];
            accelXZ[2] = event.values[2];

            myCurrentTimeMillis = System.currentTimeMillis();

            //Update the GUI display with this!
            gMeter.updatePoint(accelXZ[0], accelXZ[2]);

            //Log data if toggle is enabled
            if(isLogging == true){
                //Create a DB entry of all sensor data for this timestamp
                coordDB.beginTransaction();
                CoordData coordData = coordDB.createObject(CoordData.class);
                coordData.setTimeStamp(myCurrentTimeMillis);
                coordData.setLongitude(lastCords[0]);
                coordData.setLatitude(lastCords[1]);
                coordData.setAltitude(lastCords[2]);
                coordData.setBearing(lastCords[3]);
                coordData.setAccelX(event.values[0]);
                coordData.setAccelY(event.values[1]);
                coordData.setAccelZ(event.values[2]);
                coordDB.commitTransaction();
            }

        }
    }

    //Save current DB to file
    public void saveDBToFile() throws IOException {
        //Setup CSV file writing stuffs
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        Date currentTime = Calendar.getInstance().getTime();
        String fileName = currentTime + " LogData.csv";
        String filePath = baseDir + File.separator + fileName;
        File f = new File(filePath);
        FileWriter mFileWriter = new FileWriter(filePath , true);
        CSVWriter writer;

        // File exist
        if(f.exists()&&!f.isDirectory())
        {
            mFileWriter = new FileWriter(filePath, true);
            writer = new CSVWriter(mFileWriter);
        }
        else
        {
            writer = new CSVWriter(new FileWriter(filePath));
        }

        //Create a header line in the CSV file
        String[] firstLine = {"Timestamp (ms)", "Latitude", "Longitude", "Altitude (m)", "Bearing (Degrees)", "Accel X-Axis (m/s^2)", "Accel Y-Axis (m/s^2)", "Accel Z-Axis (m/s^2)"};
        writer.writeNext(firstLine);

        // Grab all data from the DB (coordDB)
        RealmResults<CoordData> session = coordDB.where(CoordData.class).findAll();

        //Loop through the database and write to file as we go
        int i = 0;
        for(CoordData data : session){
            String[] currentLine = {Long.toString(data.getTimeStamp()), Double.toString(data.getLatitude()), Double.toString(data.getLongitude()), Double.toString(data.getAltitude()), Double.toString(data.getBearing()), Double.toString(data.getAccelX()), Double.toString(data.getAccelY()), Double.toString(data.getAccelZ())};
            //new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").formatter.format(data.getTimeStamp())
            writer.writeNext(currentLine);
            i++;
        }

        writer.close();
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
                lastCords[0] = location.getLongitude();
                lastCords[1] = location.getLatitude();
                lastCords[2] = location.getAltitude();
                lastCords[3] = location.getBearing();

                //Display the mph to the screen
                speedTB.setText(Integer.toString((int) (location.getSpeed() * CONVERSION_FACTOR)));


            }
        };
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