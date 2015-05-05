package com.tingbacke.wearmaps;

/**
 * Johan Tingbacke, 2015-04-19
 * From tutorial: http://blog.teamtreehouse.com/beginners-guide-location-android
 *
 * To add custom notifications: http://possiblemobile.com/2014/07/create-custom-ongoing-notification-android-wear/
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.wearable.Wearable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MobileActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MobileActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    NotificationManager notificationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //showNotification(1, "basic", getBasicNotification("myStack"));


        setUpMapIfNeeded();

        UiSettings mapSettings;
        mapSettings = mMap.getUiSettings();
        mapSettings.setZoomControlsEnabled(true);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5 * 1000)        // 5 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds
    }



    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        super.onDestroy();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();

            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        mMap.setMyLocationEnabled(true);
        /**
         * https://geolocation.ws/map/55.588227,13.002735/13/en?types=&limit=300&licenses=
         * Dammfrivägen 61 = 55.587116, 12.979788
         */
        mMap.addMarker(new MarkerOptions().position(new LatLng(55.61015, 12.9786)).title("Cykelpump Kockums Torg"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55.5877, 12.9887)).title("Cykelpump Malmö gamla stadion"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55.6093, 12.9967)).title("Cykelpump, Anna Linds plats"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55.6025, 12.9679)).title("Cykelpump, Ribersborgsstigen"));

        mMap.addMarker(new MarkerOptions().position(new LatLng(55.6131, 12.9767)).title("Turning Torso"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55.6140, 12.9839)).title("Stapelbäddsparken, skatepark"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55.6125, 12.9914)).title("Media Evolution City"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55.6144, 12.9895)).title("Doc Piazza Trattoria"));

        mMap.addMarker(new MarkerOptions().position(new LatLng(55.6156, 12.9857)).title("Kranen K3, Malmö Högskola"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55.6150, 12.9858)).title("Ubåtshallen"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55.6108, 12.9949)).title("Orkanen, Malmö Högskola"));

        mMap.addMarker(new MarkerOptions().position(new LatLng(55.5850, 12.9873)).title("Swedbank Stadion"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55.5922, 12.9975)).title("Pildammsparken Entré"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55.5901, 12.9887)).title("Pildammsparken Tallriken"));

        mMap.addMarker(new MarkerOptions().position(new LatLng(55.5890, 12.9825)).title("Jet bensinmack, Lorensborg"));

        mMap.addMarker(new MarkerOptions().position(new LatLng(55.6048, 12.9658)).title("Ribersborgs Kallbadhus"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55.6041, 12.9735)).title("Toalett, Ribersborgsstigen"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55.6086, 12.9774)).title("Kockum fritid"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55.6138, 12.9812)).title("Varvsparken"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55.6137, 12.9725)).title("Daniabadet, Västra Hamnen"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55.6171, 12.9744)).title("Scaniabadet, Västra Hamnen"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55.5967, 13.0053)).title("MALMÖ"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55.5986, 12.9836)).title("Kronprinsen"));
        //mMap.addMarker(new MarkerOptions().position(new LatLng()).title(""));
        //mMap.addMarker(new MarkerOptions().position(new LatLng()).title(""));
        //mMap.addMarker(new MarkerOptions().position(new LatLng()).title(""));
        //mMap.addMarker(new MarkerOptions().position(new LatLng()).title(""));


        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Get LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Get Current Location
        Location myLocation = locationManager.getLastKnownLocation(provider);

        // Get latitude of the current location
        double latitude = myLocation.getLatitude();

        // Get longitude of the current location
        double longitude = myLocation.getLongitude();

        // Create a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);

        // Show the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(17)
                .bearing(0)
                .tilt(25)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        LatLng myCoordinates = new LatLng(latitude, longitude);
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(myCoordinates, 15);
        mMap.animateCamera(yourLocation);

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (LocationListener) this);
        } else {
            handleNewLocation(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    /**
     * Updates my location to currentLocation, moves the camera and adds a marker for my location accordingly
     *
     * @param location
     */
    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        /*
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("I am here!");
        mMap.addMarker(options);
        */

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(17)
                .bearing(0)
                .tilt(25)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        TextView myLatitude = (TextView)findViewById(R.id.textView);
        TextView myLongitude = (TextView)findViewById(R.id.textView2);
        TextView myAddress = (TextView)findViewById(R.id.textView3);

        myLatitude.setText("Latitude: " + String.valueOf(currentLatitude));
        myLongitude.setText("Longitude: " + String.valueOf(currentLongitude));

        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);


        try {
            List<Address> addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);

            if(addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("Address:\n");
                for(int i=0; i<returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                myAddress.setText(strReturnedAddress.toString());

                // Added this Toast to display address ---> In order to find out where to call notification builder for wearable
                Toast.makeText(MobileActivity.this, myAddress.getText().toString(),
                        Toast.LENGTH_LONG).show();
                         showNotification(1, "basic", getBasicNotification("myStack"));

            }
            else{
                myAddress.setText("No Address returned!");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            myAddress.setText("Cannot get Address!");
        }
    }

    /**
     * Show a notification.
     *
     * @param id           The notification id
     * @param tag          The notification tag
     * @param notification The notification
     */
    private void showNotification(int id, String tag, Notification notification) {
        notificationManager.notify(tag, id, notification);
    }

    private Notification getBasicNotification(String stack) {
        String title = "Current place";
        TextView tv = (TextView) findViewById(R.id.textView3);
        String text = tv.getText().toString();

        // Här bestämmer jag vibrationsmönster
        long[] pattern = { 0, 100, 0 };

        return new Notification.Builder(this)
                .setSmallIcon(R.mipmap.icon_mdpi)
                .setContentTitle(title)
                .setContentText(text)
                //.setVibrate(pattern)
                //.setGroup(stack)
                .build();
    }

}
