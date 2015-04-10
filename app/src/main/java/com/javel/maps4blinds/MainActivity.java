package com.javel.maps4blinds;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.*;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.TilesLoadedListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends ActionBarActivity {

    private MapView mv;
    private UserLocationOverlay myLocationOverlay;
    private String currentMap = null;
    private double longitude = 0.0;
    private double latitude = 0.0;
    TextToSpeech ttobj;

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }
    };

    private LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Crashlytics.start(this);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);

        ttobj=new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            ttobj.setLanguage(Locale.getDefault());
                        }
                    }
                }
        );

        setContentView(R.layout.activity_main);

        /*mv = (MapView) findViewById(R.id.mapview);
        mv.setMinZoomLevel(mv.getTileProvider().getMinimumZoomLevel());
        mv.setMaxZoomLevel(mv.getTileProvider().getMaximumZoomLevel());
        mv.setCenter(mv.getTileProvider().getCenterCoordinate());
        mv.setZoom(0);
        currentMap = getString(R.string.streetMapId);

        // Show user location (purposely not in follow mode)
        mv.setUserLocationEnabled(true);

        mv.loadFromGeoJSONURL("https://gist.githubusercontent.com/tmcw/10307131/raw/21c0a20312a2833afeee3b46028c3ed0e9756d4c/map.geojson");
//        setButtonListeners();
        Marker m = new Marker(mv, "Edinburgh", "Scotland", new LatLng(55.94629, -3.20777));
        m.setIcon(new Icon(this, Icon.Size.SMALL, "marker-stroked", "ee8a65"));
        mv.addMarker(m);

        m = new Marker(mv, "Stockholm", "Sweden", new LatLng(59.32995, 18.06461));
        m.setIcon(new Icon(this, Icon.Size.LARGE, "city", "3887be"));
        mv.addMarker(m);

        m = new Marker(mv, "Prague", "Czech Republic", new LatLng(50.08734, 14.42112));
        m.setIcon(new Icon(this, Icon.Size.MEDIUM, "land-use", "3bb2d0"));
        mv.addMarker(m);

        m = new Marker(mv, "Athens", "Greece", new LatLng(37.97885, 23.71399));
        mv.addMarker(m);

        m = new Marker(mv, "Tokyo", "Japan", new LatLng(35.70247, 139.71588));
        m.setIcon(new Icon(this, Icon.Size.LARGE, "city", "3887be"));
        mv.addMarker(m);

        m = new Marker(mv, "Ayacucho", "Peru", new LatLng(-13.16658, -74.21608));
        m.setIcon(new Icon(this, Icon.Size.LARGE, "city", "3887be"));
        mv.addMarker(m);

        m = new Marker(mv, "Nairobi", "Kenya", new LatLng(-1.26676, 36.83372));
        m.setIcon(new Icon(this, Icon.Size.LARGE, "city", "3887be"));
        mv.addMarker(m);

        m = new Marker(mv, "Canberra", "Australia", new LatLng(-35.30952, 149.12430));
        m.setIcon(new Icon(this, Icon.Size.LARGE, "city", "3887be"));
        mv.addMarker(m);

        mv.setOnTilesLoadedListener(new TilesLoadedListener() {
            @Override
            public boolean onTilesLoaded() {
                return false;
            }

            @Override
            public boolean onTilesLoadStarted() {
                // TODO Auto-generated method stub
                return false;
            }
        });*/
//        mv.setVisibility(View.VISIBLE);

        /*Button bugsBut = changeButtonTypeface((Button) findViewById(R.id.bugsButton));
        bugsBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://github.com/mapbox/mapbox-android-sdk/issues?state=open";
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(i);
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.menuItemStreets:
                //replaceMapView(getString(R.string.streetMapId));
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                getAddressForLocation(location.getLatitude(), location.getLongitude());
                return true;
            case R.id.menuItemSatellite:
                replaceMapView(getString(R.string.satelliteMapId));
                return true;
            case R.id.menuItemTerrain:
                replaceMapView(getString(R.string.terrainMapId));
                return true;
            case R.id.menuItemOutdoors:
                replaceMapView(getString(R.string.outdoorsMapId));
                return true;
            case R.id.menuItemWoodcut:
                replaceMapView(getString(R.string.woodcutMapId));
                return true;
            case R.id.menuItemPencil:
                replaceMapView(getString(R.string.pencilMapId));
                return true;
            case R.id.menuItemSpaceship:
                replaceMapView(getString(R.string.spaceShipMapId));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void getAddressForLocation(double LATITUDE, double LONGITUDE) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);

            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder();
                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(" ");
                }
                TextView textView = (TextView)findViewById(R.id.street_name);
                String street = strReturnedAddress.toString();
                textView.setText(strReturnedAddress.toString());
                ttobj.speak(street, TextToSpeech.QUEUE_FLUSH, null);
                Log.d("Address: ", strReturnedAddress.toString());
                //et_lugar.setText(strReturnedAddress.toString());
            }
            else {
                Log.d("No Address returned!", "");
                //et_lugar.setText("No Address returned!");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d("Cannot get Address!", "");
            //et_lugar.setText("Canont get Address!");
        }
    }

    protected void replaceMapView(String layer) {

        if (TextUtils.isEmpty(layer) || TextUtils.isEmpty(currentMap) || currentMap.equalsIgnoreCase(layer)) {
            return;
        }

        ITileLayer source;
        BoundingBox box;

        source = new MapboxTileLayer(layer);

        mv.setTileSource(source);
        box = source.getBoundingBox();
        mv.setScrollableAreaLimit(box);
        mv.setMinZoomLevel(mv.getTileProvider().getMinimumZoomLevel());
        mv.setMaxZoomLevel(mv.getTileProvider().getMaximumZoomLevel());
        currentMap = layer;
/*
        mv.setCenter(mv.getTileProvider().getCenterCoordinate());
        mv.setZoom(0);
*/
    }

    private Button changeButtonTypeface(Button button) {
        return button;
    }

    public LatLng getMapCenter() {
        return mv.getCenter();
    }

    public void setMapCenter(ILatLng center) {
        mv.setCenter(center);
    }

    /**
     * Method to show settings  in alert dialog
     * On pressing Settings button will lauch Settings Options - GPS
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getBaseContext());

        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getBaseContext().startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
}
