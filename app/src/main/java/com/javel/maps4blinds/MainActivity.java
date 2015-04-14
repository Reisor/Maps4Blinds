package com.javel.maps4blinds;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Locale;

public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final String TAG = "Maps4Blinds";

    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    protected TextToSpeech ttobj;

    /**
     * Intentes for the notificaction service
     */
    private Intent mIntentService;
    private PendingIntent mPendingIntent;

    protected TextView streetName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Add the reference to the street name textview
        streetName = (TextView)findViewById(R.id.street_name);

        // Create and link the intent with the notification service
        mIntentService = new Intent(this, LocationService.class);
        mPendingIntent = PendingIntent.getService(this, 1, mIntentService, 0);

        // Initialize the google play services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // Initialize the text to speech object
        ttobj = new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            ttobj.setLanguage(Locale.getDefault());
                        }
                    }
                }
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Disconnecting the client invalidates it.
        if(mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Get the last location
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        // Change and read the textview with the new location
        onChangeStreet(location);

        // Create the request to the location updates
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(60000); // Update location every minute

        // Start the notification services
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, mPendingIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection has failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        onChangeStreet(location);
    }

    public void onButtonPressed (View v)
    {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        onChangeStreet(location);
    }

    public void onChangeStreet(Location location)
    {
        if (mLastLocation == null) { // Check if it the first time we start the app
            ChangeStreet(location);
        }
        else {
            // Update only when the location has change in 10 meters approximately
            if (Math.abs(location.getLatitude() - mLastLocation.getLatitude()) > 0.0001 ||
                    Math.abs(location.getLongitude() - mLastLocation.getLongitude()) > 0.0001) {
                ChangeStreet(location);
            }
        }
    }

    public void ChangeStreet(Location location)
    {
        String street = Utility.getAddressForLocation(this, location.getLatitude(), location.getLongitude());
        streetName.setText(street);

        ttobj.speak(street, TextToSpeech.QUEUE_FLUSH, null);

        mLastLocation = location;
    }
}

