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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
        buildGoogleApiClient();

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
                intent.putExtra(EventCreationActivity.EXTRA_EVENT_LATLNG, latlng);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause(){
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }
    protected  void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(50000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        createLocationRequest();

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mLastLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        }
        updateUI();
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
        if(mLastLatLng != null) {
            Log.i("LAST", mLastLatLng.toString());
            LatLng cameraPos = mLastLatLng;
            //LatLng testLoc = new LatLng(mLastLocation.getLatitude() + 0.0001, mLastLocation.getLongitude() + 0.0001);
            //LatLng cameraPos = new LatLng(30.0, -85.0);
            //Log.i("CAMERA POS", cameraPos.toString());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraPos, 16.6f));

            //LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(cameraPos).title("Me"));
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(cameraPos));
            Marker testMark = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(30.0, -85.0))
                    .title("test title")
                    .snippet("6:00"));

        }

    }
//    private LocationManager getLocation(){
//        // Acquire a reference to the system Location Manager
//        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        mLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//
//        // Define a listener that responds to location updates
//        LocationListener listener = new LocationListener() {
//            public void onLocationChanged(Location location) {
//                // Called when a new location is found by the network location provider.
//                //makeUseOfNewLocation(location);
//                mLastLocation = location;
//            }
//
//            public void onStatusChanged(String provider, int status, Bundle extras) {}
//
//            public void onProviderEnabled(String provider) {}
//
//            public void onProviderDisabled(String provider) {}
//        };
//
//        // Register the listener with the Location Manager to receive location updates
//
//        long minTime = 1000;
//        float minDistance = 1;
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, listener);
//
//        Log.i("LAST KNOWN LOCATION", mLastLocation.toString());
//        return locationManager;
//    }

    private void request(int method, String url){
        // Request a string response
        JsonObjectRequest stringRequest = new JsonObjectRequest(method, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //event = response;
                        try{
                            Log.i("event response", response.toString());
                            response.getString("title");
                            //title.setText(event.getString("title"));
                            //picture.setProfileId(event.getString("facebook_created_by"));
                            //coordinator.setText(event.getString("fname") + event.getString("lname"));
                            //   description.setText(event.getString("description"));
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
