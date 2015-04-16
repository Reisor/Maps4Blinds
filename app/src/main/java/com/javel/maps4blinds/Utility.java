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

            if (addresses != null) { // if there is an addres
                // Obtain the full address
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder();

                // Get the address without the postal code and city
                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex() - 1; i++) {
                    writeLog(TAG, returnedAddress.getAddressLine(i));
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(" ");
                }

                street = strReturnedAddress.toString();

                writeLog(TAG, "Address: " + street);
            }
            else {
                writeLog(TAG, "No Address returned!");
                return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
            writeLog(TAG, "Cannot get Address!");
            return "Error";
        }

        return street;
    }

    /**
     * Write a log message if is in Debug mode.
     *
     * @param tag String of the class
     * @param s Message to log
     */
    public static void writeLog(String tag, String s) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, s);
        }
    }
}
