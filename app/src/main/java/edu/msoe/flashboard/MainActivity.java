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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    /**
     * This method is run when the app is first launched and sets everything up
     *
     * @param savedInstanceState the current saved state of the app
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
}