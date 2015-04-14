package com.javel.maps4blinds;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Utility {

    private static final String TAG = "Utility";

    public static String getAddressForLocation(Context context, double LATITUDE, double LONGITUDE) {
        // Initialize the geocoder with the default locale
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        String street;

        try {
            // Get the street in the given coordinates
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);

            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder();
                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(" ");
                }

                street = strReturnedAddress.toString();

                Log.i(TAG, "Address: " + street);
            }
            else {
                Log.i(TAG, "No Address returned!");
                return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "Cannot get Address!");
            return "Error";
        }

        return street;
    }
}
