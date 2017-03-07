package edu.hanover.basin;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.event.Event;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import com.google.android.gms.location.LocationListener;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private LatLng mLastLatLng;
    private GoogleApiClient mGoogleApiClient;
    private Map<Marker, JSONObject> allMarkerMap;
    private Marker mMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        // Create an instance of GoogleAPIClient.
        getLocation();


        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        //Get event data. Locations are all that's needed for now.
        //How do I start around a location?
        //Will need to limit based on proximity
        //mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        // Add a marker in Sydney and move the camera
        //getLocation();

        //mMap.setMaxZoomPreference(20.0f);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {

                Log.d("", marker.getTitle());
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener(){

            @Override public void onMapLongClick(LatLng latlng){
                //need to make confirmation box
                Intent intent = new Intent(MapsActivity.this, EventCreationActivity.class);
                Double lat = latlng.latitude;
                Double lng = latlng.longitude;
                intent.putExtra(EventCreationActivity.EXTRA_EVENT_LAT, lat);
                intent.putExtra(EventCreationActivity.EXTRA_EVENT_LNG, lng);
                intent.putExtra(EventCreationActivity.EXTRA_METHOD, false);
                startActivity(intent);
            }
        });

        basinURL url = new basinURL();
        url.getEventURL("");
        request(Request.Method.GET, url.toString());
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
    protected void onPause(){
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }
    protected  void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(500);
        mLocationRequest.setFastestInterval(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void getLocation(){
        createLocationRequest();
        buildGoogleApiClient();

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mLastLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            updateUI();
        }
        else{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i){

    }

    @Override
    public void onConnectionFailed(ConnectionResult result){
        buildGoogleApiClient();

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        //mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }

    private void buildGoogleApiClient(){
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void updateUI(){
        //test location
        //mLastLatLng = new LatLng(38.713, -85.459 );
        if(mLastLatLng != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            Toast.makeText(this, "location :"+ mLastLatLng, Toast.LENGTH_SHORT).show();
            Log.i("LAST", mLastLatLng.toString());
            LatLng cameraPos = mLastLatLng;
            //LatLng testLoc = new LatLng(mLastLocation.getLatitude() + 0.0001, mLastLocation.getLongitude() + 0.0001);
            //LatLng cameraPos = new LatLng(30.0, -85.0);
            //Log.i("CAMERA POS", cameraPos.toString());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraPos, 16.6f));

            //LatLng sydney = new LatLng(-34, 151);
            if(mMe != null){
                mMe.setPosition(cameraPos);
            }
            else{
                mMe = mMap.addMarker(new MarkerOptions().position(cameraPos).title("Me"));
            }
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(cameraPos));
            Marker testMark = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(30.0, -85.0))
                    .title("test title")
                    .snippet("6:00"));

        }
        else{
            Toast.makeText(this, "No location; using default", Toast.LENGTH_SHORT).show();
            mLastLatLng = new LatLng(38.713, -85.459 ); //Default is Hanover
            updateUI();

        }


    }

    private void addMarkers(JSONArray events){
        JSONObject event;
        Marker marker;
        Double lat;
        Double lng;
        LatLng location;
        String title, time_date;

       try{
           for(int i = 0; i < events.length(); i++){
               event = events.getJSONObject(i);
               lat = event.getDouble("lat_coord");
               lng = event.getDouble("long_coord");
               if(lat != null && lng != null) {
                   location = new LatLng(lat, lng);
                   title = event.getString("title");
                   time_date = event.getString("time_start") + ", " + event.getString("date");


                   marker = mMap.addMarker(new MarkerOptions()
                           .position(location)
                           .title(title)
                           .snippet(time_date));
                   allMarkerMap.put(marker, event);
               }

           }
       }
       catch(JSONException e){
           Log.e("ERROR ADDING MARKERS", e.toString());
       }
    }

    private void request(int method, String url){
        // Request a string response
        JsonObjectRequest stringRequest = new JsonObjectRequest(method, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //event = response;
                        try{
                            Log.i("event response", response.toString());
                            JSONArray events = response.getJSONArray("events");


                        }
                        catch(JSONException e){
                            Log.e("JSON EXCEPTION", e.toString());
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // Error handling
                Log.e("Volley error", error.toString());
                error.printStackTrace();
            }
        });

        // Add the request to the queue
        Volley.newRequestQueue(this).add(stringRequest);

    }


}
