package com.tingbacke.wearmaps;

/**
 * Johan Tingbacke, 2015-04-19
 * From tutorial: http://blog.teamtreehouse.com/beginners-guide-location-android
 * <p/>
 * To add custom notifications: http://possiblemobile.com/2014/07/create-custom-ongoing-notification-android-wear/
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.IntentSender;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MobileActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MobileActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    public NotificationManager notificationManager;

    private Marker[] placeMarkers;
    private final int MAX_PLACES = 20;
    private MarkerOptions[] places;

    ArrayList<LatLng> markerPoints;

    // Variable used for calculating current distance to destination
    public float currDistance;
    // Hardcoded LatLng location for a destination. Ribersborgs Kallbadhus.
    public double destLat = 55.6048;
    public double destLong = 12.9658;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile);

        setUpMapIfNeeded();

        UiSettings mapSettings;
        mapSettings = mMap.getUiSettings();
        mapSettings.setZoomControlsEnabled(true);
        mapSettings.setCompassEnabled(true);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)        // 5 seconds, in milliseconds
                .setFastestInterval(1000); // 1 second, in milliseconds

        // Initializing
        markerPoints = new ArrayList<LatLng>();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
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
    protected void onPause() {
        super.onPause();
        setUpMapIfNeeded();
        mGoogleApiClient.isConnected();
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

                placeMarkers = new Marker[MAX_PLACES];
                // Setting onclick event listener for the map
                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

                    @Override
                    public void onMapLongClick(LatLng point) {

                        // Already two locations
                        if (markerPoints.size() > 1) {
                            markerPoints.clear();
                            mMap.clear();
                        }

                        // Adding new item to the ArrayList
                        markerPoints.add(point);

                        // Creating MarkerOptions
                        MarkerOptions options = new MarkerOptions();

                        // Setting the position of the marker
                        options.position(point);

                        //For the start/end location, marker colors are blue/green
                        if (markerPoints.size() == 1) {
                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        } else if (markerPoints.size() == 2) {
                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        }

                        // Add new marker to the Google Map Android API V2
                        mMap.addMarker(options);

                        // Checks, whether start and end locations are captured
                        if (markerPoints.size() >= 2) {
                            LatLng origin = markerPoints.get(0);
                            LatLng dest = markerPoints.get(1);

                            // Getting URL to the Google Directions API
                            String url = getDirectionsUrl(origin, dest);

                            GetPlaces downloadTask = new GetPlaces();

                            // Start downloading json data from Google Directions API
                            downloadTask.execute(url);
                        }
                    }


                });
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

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setMyLocationEnabled(true);
        /**
         * https://geolocation.ws/map/55.588227,13.002735/13/en?types=&limit=300&licenses=
         * Dammfrivägen 61 = 55.587116, 12.979788
         */
        /*
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
*/
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
                .zoom(18)
                .bearing(0)
                .tilt(25)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        LatLng myCoordinates = new LatLng(latitude, longitude);
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(myCoordinates, 18);
        mMap.animateCamera(yourLocation);

    }

    /**
     * Updates my location to currentLocation, moves the camera.
     *
     * @param location
     */
    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(18)
                .bearing(0)
                .tilt(25)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        TextView myLatitude = (TextView) findViewById(R.id.textView);
        TextView myLongitude = (TextView) findViewById(R.id.textView2);
        TextView myAddress = (TextView) findViewById(R.id.textView3);
        ImageView myImage = (ImageView) findViewById(R.id.imageView);

        myLatitude.setText("Latitude: " + String.valueOf(latitude));
        myLongitude.setText("Longitude: " + String.valueOf(longitude));

        // Instantiating currDistance to get distance to destination from my location
        currDistance = getDistance(latitude, longitude, destLat, destLong);

        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");
                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }

                // Hardcoded string which searches through strReturnedAddress for specifics
                // if statements decide which information will be displayed
                String fake = strReturnedAddress.toString();

                if (fake.contains("Östra Varvsgatan")) {

                    myAddress.setText("K3, Malmö Högskola" + "\n" + "Cykelparkering: 50m" + "\n" + "Dest: " + Math.round(currDistance) + "m away");
                    myImage.setImageResource(R.mipmap.kranen);


                } else if (fake.contains("Lilla Varvsgatan")) {

                    myAddress.setText("Turning Torso" + "\n" + "Café: 90m " + "\n" + "Dest: " + Math.round(currDistance) + "m away");
                    myImage.setImageResource(R.mipmap.turning);

                } else if (fake.contains("Västra Varvsgatan")) {

                    myAddress.setText("Kockum Fritid" + "\n" + "Bankomat: 47m" + "\n" + "Dest: " + Math.round(currDistance) + "m away");
                    myImage.setImageResource(R.mipmap.kockum);

                } else if (fake.contains("Stapelbäddsgatan")) {

                    myAddress.setText("Stapelbäddsparken" + "\n" + "Toalett: 63m" + "\n" + "Dest: " + Math.round(currDistance) + "m away");
                    myImage.setImageResource(R.mipmap.stapel);

                } else if (fake.contains("Stora Varvsgatan")) {

                    myAddress.setText("Media Evolution City" + "\n" + "Busshållplats: 94m" + "\n" + "Dest: " + Math.round(currDistance) + "m away");
                    myImage.setImageResource(R.mipmap.media);

                } else if (fake.contains("Masttorget")) {

                    myAddress.setText("Ica Maxi" + "\n" + "Systembolaget: 87m" + "\n" + "Dest: " + Math.round(currDistance) + "m away");
                    myImage.setImageResource(R.mipmap.ica);

                } else if (fake.contains("Riggaregatan")) {

                    myAddress.setText("Scaniabadet" + "\n" + "\n" + "Dest: " + Math.round(currDistance) + "m away");
                    myImage.setImageResource(R.mipmap.scaniabadet);

                } else if (fake.contains("Dammfrivägen")) {
//fakear Turning Torso på min adress för tillfället
                    myAddress.setText("Turning Torso" + "\n" + "453m away");
                    myImage.setImageResource(R.mipmap.turning);
                }

                //myAddress.setText(strReturnedAddress.toString()+ "Dest: " + Math.round(currDistance) + "m away");
/*
                // Added this Toast to display address ---> In order to find out where to call notification builder for wearable
                Toast.makeText(MobileActivity.this, myAddress.getText().toString(),
                        Toast.LENGTH_LONG).show();
*/
                //Here is where I call the notification builder for the wearable
                showNotification(1, "basic", getBasicNotification("myStack"));

            } else {
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
        String title = "Västra Hamnen";
        TextView tv = (TextView) findViewById(R.id.textView3);
        String text = tv.getText().toString();

        // Here I determine the vibration pattern for the notification
        long[] pattern = {0, 200, 0};

/*
        // This seems only to be for adding longer texts to a notification...
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
*/
        /*
        Spannable sb = new SpannableString("Bold this and italic that.");
        sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 14, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
*/

        NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender()
                .setBackground(BitmapFactory.decodeResource(getResources(), R.mipmap.turn));

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.addLine(Html.fromHtml("<b>Turning Torso</b>"));
        inboxStyle.addLine(Html.fromHtml("<b>453m away</b>"));

        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.icon_mdpi)
                .setContentTitle(title)
                .setContentText(text)
                //.setStyle(bigPic)
                //.setStyle(inboxStyle)
                        .extend(extender)
                        //.setStyle(bigTextStyle)
                .setVibrate(pattern)
                .setGroup(stack)
                .build();
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

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        //Denna tog tid att förstå sig på, men nu jävlar!
        // Change travel mode
        String tr_mode = "mode=bicycling";

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + "&" + tr_mode + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception while dl url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class GetPlaces extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {
            // Fetch places
            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(3);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        }
    }

    // Method for calculating distance between current position and destination
    public float getDistance(double lat1, double longi, double lat2,
                             double longi2) {

        float[] dist = new float[1];

        try {
            Location.distanceBetween(lat1, longi, lat2, longi2, dist);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Making a toast of the returned value, rounding it using Math.round() to get an "int"
        Toast.makeText(getApplicationContext(), String.valueOf(Math.round(dist[0])) + " meters away",
                Toast.LENGTH_LONG).show();
        return dist[0];
    }


}