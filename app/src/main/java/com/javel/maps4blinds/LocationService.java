package com.javel.maps4blinds;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderApi;

public class LocationService extends IntentService
{
    private final String TAG = "LocationService";

    public LocationService() {
        super("LocationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Location location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);

        if (location != null) {
            Log.i(TAG, "Location: " + location.getLatitude() + "," + location.getLongitude());

            String street = Utility.getAddressForLocation(this, location.getLatitude(), location.getLongitude());

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Builder builder = new NotificationCompat.Builder(this);
            builder.setContentTitle("Maps4Blinds");
            builder.setContentText(street);
            builder.setSmallIcon(R.mipmap.ic_launcher);

            notificationManager.notify(1234, builder.build());
        }
    }
}
