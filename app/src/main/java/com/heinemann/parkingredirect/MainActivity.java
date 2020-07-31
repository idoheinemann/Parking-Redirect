package com.heinemann.parkingredirect;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String LOCATION_PREFS_INDEX = "location";

    public static final int PERMISSION_REQUEST_LOCATION_SAVE = 99;

    public boolean checkLocationPermission(int request) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        PERMISSION_REQUEST_LOCATION_SAVE);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        request);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION_SAVE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        saveLocationToSharedPrefs();
                    }

                }
            }

        }
    }

    public void saveLocationToSharedPrefs() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (location == null) {
            location = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }
        if (location == null) {
            Toast.makeText(MainActivity.this, "Unable to get location", Toast.LENGTH_LONG).show();
            return;
        }
        String locString = location.getLatitude() + "," + location.getLongitude();
        if (!MainActivity.this.getPreferences(MODE_PRIVATE).edit()
                .putString(LOCATION_PREFS_INDEX, locString)
                .commit()) {
            Toast.makeText(MainActivity.this, "Location saving failed, please try again later", Toast.LENGTH_LONG).show();
        }
        final TextView text = (TextView) findViewById(R.id.text);
        text.setText("Last Saved Location: " + locString);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView text = (TextView) findViewById(R.id.text);
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);

        text.setText("Last Saved Location: " + prefs.getString(LOCATION_PREFS_INDEX, "Unknown"));

        Button button = (Button) findViewById(R.id.save);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!checkLocationPermission(PERMISSION_REQUEST_LOCATION_SAVE)) {
                    Toast.makeText(MainActivity.this, "You must allow location service to use this app", Toast.LENGTH_LONG).show();
                    return;
                }
                saveLocationToSharedPrefs();
            }
        });

        button = (Button) (findViewById(R.id.nav));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = MainActivity.this.getPreferences(MODE_PRIVATE);
                String url = "https://www.google.com/maps/search/?api=1&query=" +
                        prefs.getString(LOCATION_PREFS_INDEX, "0.0,0.0");

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });
    }
}
