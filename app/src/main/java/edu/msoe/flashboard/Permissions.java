package edu.msoe.flashboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

// This class exists soley to get permissions in a blocking fashion
public class Permissions extends AppCompatActivity {

    private static final int PERMISSIONS_ALL = 1;
    private static final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //Check to ensure necessary permissions provided
        if (!hasPermissions()) {
            //Get permissions
            Toast.makeText(this, "Please allow permissions if you haven't already", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_ALL);
        } else{
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finishAffinity();
        }



        findViewById(R.id.startApp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finishAffinity();



            }
        });

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