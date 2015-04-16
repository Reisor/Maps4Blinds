package com.javel.maps4blinds;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.location.Location;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import com.google.android.gms.location.FusedLocationProviderApi;

import java.util.Locale;

public class LocationService extends IntentService
{
    private final String TAG = "LocationService";

    protected TextToSpeech mTts;

    public LocationService() {
        super("LocationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Location location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);

        if (mTts == null) {
            mTts = new TextToSpeech(getApplicationContext(),
                    new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status != TextToSpeech.ERROR) {
                                mTts.setLanguage(Locale.getDefault());
                            }
                        }
                    }
            );
        }

        if (location != null) {
            String street = Utility.getAddressForLocation(this, location.getLatitude(), location.getLongitude());

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Builder builder = new NotificationCompat.Builder(this);
            builder.setContentTitle("Maps4Blinds");
            builder.setContentText(street);
            builder.setSmallIcon(R.mipmap.ic_launcher);

            notificationManager.notify(1234, builder.build());

            textToSpeech(street);
        }
    }

    public void textToSpeech(String street) {
        mTts.speak(street, TextToSpeech.QUEUE_FLUSH, null);
    }

}
