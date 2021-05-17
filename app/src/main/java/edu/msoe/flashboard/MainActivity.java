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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import android.view.WindowManager;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;


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
    public Realm coordDB;
    private LocationCallback locationCallback;
    private static final float CONVERSION_FACTOR = 2.23694f;
    private TextView speedTB;
    private GMeter gMeter;
    private float[] accelXZ;
    private boolean isLogging = false;

    /**
     * This method is run when the app is first launched and sets everything up
     *
     * @param savedInstanceState the current saved state of the app
     */
    @SuppressLint({"MissingPermission", "UseSwitchCompatOrMaterialCode"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        gMeter = findViewById(R.id.g_meter);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Switch simpleSwitch = findViewById(R.id.switch_logging);
        speedTB = findViewById(R.id.speedTB);

        Realm.init(this);
        coordDB = Realm.getDefaultInstance();
        accelXZ = new float[4];

        FusedLocationProviderClient flpc = LocationServices.getFusedLocationProviderClient(this);

        //Register accelerometer sensor
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_UI);

        //Register location requests
        LocationRequest locationRequest = LocationRequest.create();
        // 0 = literally as fast as it can
        locationRequest.setInterval(0);
        // We don't really need this, in fact we want as steady as possible
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        startLocationUpdates();
        flpc.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        //Listener for Logging Data switch/toggle
        findViewById(R.id.switch_logging).setOnClickListener(view -> {
            //Save log to file if logging enabled -> logging disabled
            if (isLogging) {
                //Save current log data to file
                try {
                    saveDBToFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //Set logging flag to false
            }  // Enable logging if disabled -> enabled
            //Set logging flag to true
            isLogging = simpleSwitch.isChecked();
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

            //Update the GUI display with this!
            gMeter.updatePoint(accelXZ[0], accelXZ[2]);
        }
    }

    /**
     * Saves DB info to a CSV file on the device
     *
     * @throws IOException since files are being manipulated an IOException is possible
     */
    public void saveDBToFile() throws IOException {
        Context context = getApplicationContext();

        //Setup CSV file writing stuffs
        String baseDir = context.getExternalFilesDir(null).getAbsolutePath();
        Date currentTime = Calendar.getInstance().getTime();
        String fileName = currentTime + " LogData.csv";
        String filePath = baseDir + File.separator + fileName;
        File f = new File(filePath);
        new FileWriter(filePath, true);
        FileWriter mFileWriter;
        CSVWriter writer;

        // File exist
        if (f.exists() && !f.isDirectory()) {
            mFileWriter = new FileWriter(filePath, true);
            writer = new CSVWriter(mFileWriter);
        } else {
            writer = new CSVWriter(new FileWriter(filePath));
        }
        //Create a header line in the CSV file
        String[] firstLine = {"Timestamp (ms)", "Latitude", "Longitude", "Altitude (m)", "Bearing (Degrees)",
                "Speed (mph)", "Accel X-Axis (m/s^2)", "Accel Y-Axis (m/s^2)", "Accel Z-Axis (m/s^2)"};
        writer.writeNext(firstLine);
        // Grab all data from the DB (coordDB)
        RealmResults<CoordData> session = coordDB.where(CoordData.class).findAll();

        //Loop through the database and write to file as we go
        for (CoordData data : session) {
            String[] currentLine = {data.getTimeStamp(), Double.toString(data.getLatitude()),
                    Double.toString(data.getLongitude()), Double.toString(data.getAltitude()),
                    Double.toString(data.getBearing()),
                    Double.toString(data.getSpeed()), Double.toString(data.getAccelX()),
                    Double.toString(data.getAccelY()), Double.toString(data.getAccelZ())};
            //new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").formatter.format(data.getTimeStamp())
            writer.writeNext(currentLine);
        }
        writer.close();

        Toast.makeText(this, "Log files saved to: " + filePath, Toast.LENGTH_LONG).show();
    }

    /**
     * Method to update sensor accuracy (Not implemented in this project)
     *
     * @param sensor   reference to the sensor being targeted
     * @param accuracy the numerical value for the sensors' accuracy
     */
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
                //Display the mph to the screen
                speedTB.setText(Integer.toString((int) (location.getSpeed() * CONVERSION_FACTOR)));

                //Log data if toggle is enabled
                if (isLogging) {
                    //Create a DB entry of all sensor data for this timestamp
                    coordDB.beginTransaction();
                    CoordData coordData = coordDB.createObject(CoordData.class);
                    coordData.setTimeStamp(new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
                    coordData.setLongitude(location.getLongitude());
                    coordData.setLatitude(location.getLatitude());
                    coordData.setAltitude(location.getAltitude());
                    coordData.setBearing(location.getBearing());
                    coordData.setSpeed((location.getSpeed() * CONVERSION_FACTOR));
                    coordData.setAccelX(accelXZ[0]);
                    coordData.setAccelY(accelXZ[1]);
                    coordData.setAccelZ(accelXZ[2]);
                    coordDB.commitTransaction();
                }
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
        if (item.getItemId() == R.id.item_navMap) {
            //Launch GMaps Activity
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.item_clearDB) {
            //Clear out the database
            Toast.makeText(this, "Database Cleared!", Toast.LENGTH_LONG).show();
            RealmResults<CoordData> session = coordDB.where(CoordData.class).findAll();
            coordDB.beginTransaction();
            session.deleteAllFromRealm();
            coordDB.commitTransaction();
        }
        return true;
    }
}