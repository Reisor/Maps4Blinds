package com.javel.maps4blinds;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Locale;

public class StreetsFragment extends Fragment implements
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener

{

    private final String TAG = "StreetsFragment";

    private Context activityContext;

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

    /// Notification interval
    private int notificationTime;

    /// Notification interval multiplier
    private int notificationTimeMultiplier;

    /// Street name text view
    protected TextView mStreetName;

    /// Service connection handler button
    protected Button mButtonService;

    /// Service connection status
    protected Boolean mServiceStart;

    public StreetsFragment ()
    {
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState)
    {
        View rootView = inflater.inflate (R.layout.fragment_main, container, false);

        activityContext = this.getActivity ();

        notificationTime = 60;

        notificationTimeMultiplier = 1000;

        // Add the reference to the street name textview
        mStreetName = (TextView) rootView.findViewById (R.id.street_name);

        // Create and link the intent with the notification service
        mIntentService = new Intent(getActivity ().getApplicationContext (), LocationService.class);
        mPendingIntent = PendingIntent.getService(getActivity ().getApplicationContext (), 1, mIntentService, 0);

        // Initialize the google play services
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity ().getApplicationContext ())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // Initialize the text to speech object
        mTextToSpeech = new TextToSpeech(getActivity ().getApplicationContext (),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            mTextToSpeech.setLanguage(Locale.getDefault ());
                        }
                    }
                }
        );

        mServiceStart = false;

        Button button = (Button) rootView.findViewById (R.id.button_find);
        button.setOnClickListener (this);

        mButtonService = (Button) rootView.findViewById (R.id.button_service);
        mButtonService.setOnClickListener (this);

        return rootView;
    }

    @Override
    public void onResume ()
    {
        super.onResume ();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activityContext);

        String time = settings.getString ("pref_time_notification_key", "60");
    }

    @Override
    public void onClick (View v)
    {
        switch (v.getId ())
        {
            case R.id.button_find:
                onButtonFindPressed ();
                break;
            case R.id.button_service:
                onButtonServicePressed ();
                break;
            default:
                Utility.writeLog (TAG, "Unknown button id");
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Connect the client.
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Disconnecting the client invalidates it.
        if(mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected (Bundle bundle)
    {
        // Get the last location
        Location location = LocationServices.FusedLocationApi.getLastLocation (mGoogleApiClient);

        // Change and read the textview with the new location
        onChangeStreet (location);
    }

    @Override
    public void onConnectionSuspended (int i)
    {
        Utility.writeLog (TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed (ConnectionResult connectionResult)
    {
        Utility.writeLog (TAG, "GoogleApiClient connection has failed");
    }

    @Override
    public void onLocationChanged (Location location)
    {
        onChangeStreet (location);
    }

    public void onButtonFindPressed ()
    {
        Location location = LocationServices.FusedLocationApi.getLastLocation (mGoogleApiClient);

        onChangeStreet (location);
    }

    public void onButtonServicePressed ()
    {
        if (mServiceStart)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates (mGoogleApiClient,
                    mPendingIntent);

            mButtonService.setText (R.string.button_service_on);

            mServiceStart = false;
        } else
        {
            // Create the request to the location updates
            mLocationRequest = LocationRequest.create ();
            mLocationRequest.setPriority (LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval (notificationTime * notificationTimeMultiplier); // Update location every minute

            // Start the notification services
            LocationServices.FusedLocationApi.requestLocationUpdates (mGoogleApiClient,
                    mLocationRequest, mPendingIntent);

            mButtonService.setText (R.string.button_service_off);

            mServiceStart = true;
        }
    }

    /**
     * Check if the location has change and call the changeStreet method
     *
     * @param location Current location
     */
    public void onChangeStreet (Location location)
    {
        if (mLastLocation == null)
        { // Check if it the first time we start the app
            changeStreet (location);
        } else
        {
            // Update only when the location has change in 10 meters approximately
            if (Math.abs (location.getLatitude () - mLastLocation.getLatitude ()) > 0.0001 ||
                    Math.abs (location.getLongitude () - mLastLocation.getLongitude ()) > 0.0001)
            {
                changeStreet (location);
            }
        }
    }

    /**
     * Change the street text view and reproduce it.
     *
     * @param location Current location
     */
    public void changeStreet (Location location)
    {
        String street = Utility.getAddressForLocation (activityContext,
                location.getLatitude (), location.getLongitude ());
        mStreetName.setText (street);

        mTextToSpeech.speak (street, TextToSpeech.QUEUE_FLUSH, null);

        mLastLocation = location;
    }

    public void onNotificationTimeChange (int time)
    {
        notificationTime = time;

        if (mServiceStart)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates (mGoogleApiClient,
                    mPendingIntent);
        }

        // Create the request to the location updates
        mLocationRequest = LocationRequest.create ();
        mLocationRequest.setPriority (LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval (notificationTime * notificationTimeMultiplier); // Update location every minute

        // Start the notification services
        LocationServices.FusedLocationApi.requestLocationUpdates (mGoogleApiClient,
                mLocationRequest, mPendingIntent);

        mButtonService.setText (R.string.button_service_off);

        mServiceStart = true;
    }

}