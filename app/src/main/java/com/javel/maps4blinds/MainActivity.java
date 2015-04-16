package com.javel.maps4blinds;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
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

    /// Google api client object
    private GoogleApiClient mGoogleApiClient;

    /// Location request
    private LocationRequest mLocationRequest;

    /// Represents a geographical location
    protected Location mLastLocation;

    /// Text to speech object
    public TextToSpeech mTextToSpeech;

    /// Intent to Location Service class
    private Intent mIntentService;

    /// Pending intent of intent service
    private PendingIntent mPendingIntent;

    /// Street name text view
    protected TextView mStreetName;

    /// Service connection handler button
    protected Button mButtonService;

    /// Service connection status
    protected Boolean mServiceStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Add the reference to the street name textview
        mStreetName = (TextView)findViewById(R.id.street_name);
        mButtonService = (Button)findViewById(R.id.button_service);

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
        mTextToSpeech = new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            mTextToSpeech.setLanguage(Locale.getDefault());
                        }
                    }
                }
        );

        mServiceStart = false;
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

        mServiceStart = true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Utility.writeLog(TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Utility.writeLog(TAG, "GoogleApiClient connection has failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        onChangeStreet(location);
    }

    public void onButtonFindPressed (View v)
    {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        onChangeStreet(location);
    }

    public void onButtonServicePressed (View v)
    {
        if (mServiceStart){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
                    mPendingIntent);

            mButtonService.setText(R.string.button_service_on);

            mServiceStart = false;
        }
        else {
            // Create the request to the location updates
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(60000); // Update location every minute

            // Start the notification services
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, mPendingIntent);

            mButtonService.setText(R.string.button_service_off);

            mServiceStart = true;
        }
    }

    /**
     * Check if the location has change and call the changeStreet method
     *
     * @param location Current location
     */
    public void onChangeStreet(Location location)
    {
        if (mLastLocation == null) { // Check if it the first time we start the app
            changeStreet(location);
        }
        else {
            // Update only when the location has change in 10 meters approximately
            if (Math.abs(location.getLatitude() - mLastLocation.getLatitude()) > 0.0001 ||
                    Math.abs(location.getLongitude() - mLastLocation.getLongitude()) > 0.0001) {
                changeStreet(location);
            }
        }
    }

    /**
     * Change the street text view and reproduce it.
     *
     * @param location Current location
     */
    public void changeStreet(Location location)
    {
        String street = Utility.getAddressForLocation(this, location.getLatitude(), location.getLongitude());
        mStreetName.setText(street);

        mTextToSpeech.speak(street, TextToSpeech.QUEUE_FLUSH, null);

        mLastLocation = location;
    }
}

