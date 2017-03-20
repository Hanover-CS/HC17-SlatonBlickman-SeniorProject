package edu.hanover.basin;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListPopupWindow;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static final String EXTRA_EVENT_LAT = "EventLat";
    public static final String EXTRA_EVENT_LNG = "EventLng";

    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private LatLng mLastLatLng;
    private GoogleApiClient mGoogleApiClient;
    private Map<Marker, EventMarker> allMarkerMap;
    private EventMarker mMe;
    // Declare a variable for the cluster manager.
    private ClusterManager<EventMarker> mClusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create an instance of GoogleAPIClient.
        getLocation();

        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_lists:
                intent = new Intent(MapsActivity.this, UserEventsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_home:
                intent = new Intent(MapsActivity.this, LoginActivity.class);
                intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        allMarkerMap =  new HashMap<>();
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener(){

            @Override public void onMapLongClick(LatLng latlng){
                //need to make confirmation box
                Intent intent = new Intent(MapsActivity.this, EventCreationActivity.class);
                Double lat = latlng.latitude;
                Double lng = latlng.longitude;
                intent.putExtra(EventCreationActivity.EXTRA_EVENT_LAT, lat);
                intent.putExtra(EventCreationActivity.EXTRA_EVENT_LNG, lng);
                intent.putExtra(EventCreationActivity.EXTRA_UPDATING, false);

                intent.setFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });

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

    @Override
    protected void onResume(){
        super.onResume();
    }

    protected void createLocationRequest(){
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
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        catch(SecurityException e){
            Log.e("Security exception", e.toString());
        }

        if (mLastLocation != null) {
            mLastLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            //add rest of markers from database
            basinURL url = new basinURL();
            url.getEventURL("");
            if(allMarkerMap != null){
                request(Request.Method.GET, url.toString());
                updateUI();
            }

        }
//        else{
//            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//        }
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

    private void setupCluster(){
        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<EventMarker>(this, mMap);
        EventRenderer eventRenderer = new EventRenderer(this, mMap, mClusterManager);
        mClusterManager.setRenderer(eventRenderer);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);

        //mClusterManager.setRenderer(new EventMarkerRenderer(this, mMap, mClusterManager));
        mClusterManager
                .setOnClusterClickListener(new ClusterManager.OnClusterClickListener<EventMarker>() {
                    @Override
                    public boolean onClusterClick(final Cluster<EventMarker> cluster) {
                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MapsActivity.this);
                        ArrayList<EventMarker> eventMarkers;
                        eventMarkers = ArrayUtil.toArrayList(cluster);

                        builderSingle.setNegativeButton("Back to Map", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                dialog.dismiss();
                            }
                        });
                        builderSingle.setPositiveButton("New Event", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                Collection<EventMarker> collection = cluster.getItems();
                                EventMarker marker = collection.iterator().next();
                                LatLng position = marker.getPosition();
                                Intent intent = new Intent(getApplicationContext(), EventCreationActivity.class);
                                intent.putExtra(EventCreationActivity.EXTRA_EVENT_LAT, position.latitude);
                                intent.putExtra(EventCreationActivity.EXTRA_EVENT_LNG, position.longitude);
                                intent.putExtra(EventCreationActivity.EXTRA_UPDATING, false);
                                startActivity(intent);
                            }
                        });
                        // Create the adapter to convert the array to views
                        final EventMarkersAdapter adapter = new EventMarkersAdapter(getApplicationContext(), eventMarkers);
                        builderSingle.setAdapter(adapter,  new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                EventMarker event = adapter.getItem(item);
                                Intent intent = new Intent(getApplicationContext(), EventDetailsActivity.class);
                                intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, event.getID());
                                intent.setFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                startActivity(intent);
                            }
                        });
                        builderSingle.show();

                        return true;
                    }
                });


        mClusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<EventMarker>() {
            @Override
            public void onClusterItemInfoWindowClick(EventMarker eventMarker) {
                if(!(eventMarker.getID().equals("-1"))){
                    Intent intent = new Intent(getApplicationContext(), EventDetailsActivity.class);
                    intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, eventMarker.getID());
                    intent.setFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(intent);
                }
            }
        });
    }

    private void updateUI(){
        //test location
        //mLastLatLng = new LatLng(38.713, -85.459 );
        if(mLastLatLng != null) {
            if(mClusterManager == null){
                setupCluster();
            }

            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            Toast.makeText(this, "location :"+ mLastLatLng, Toast.LENGTH_SHORT).show();
            Log.i("LAST LOCATION", mLastLatLng.toString());


            if(mMe == null){
                mMe = new EventMarker(mLastLatLng.latitude, mLastLatLng.longitude, "Me", "", "-1");
                mClusterManager.addItem(mMe);
            }

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
        String title, time_date, id;
        EventMarker eventMarker;

        mMe = new EventMarker(mLastLatLng.latitude, mLastLatLng.longitude, "Me", "", "-1");
        mClusterManager.addItem(mMe);

        try{
           for(int i = 0; i < events.length(); i++){
               event = events.getJSONObject(i);
               lat = event.getDouble("lat_coord");
               lng = event.getDouble("long_coord");
               id = event.getString("_id");
               location = new LatLng(lat, lng);
               title = event.getString("title");
               time_date = event.getString("time_start") + ", " + event.getString("date");

               eventMarker = new EventMarker(lat, lng, title, time_date, id);
               //allMarkerMap.put(marker, event);
               mClusterManager.addItem(eventMarker);

           }
        }
        catch(JSONException e){
           Log.e("ERROR ADDING MARKERS", e.toString());
        }
    }

    private void request(int method, String url){
        // Request a jsonObject response
        JsonObjectRequest objRequest = new JsonObjectRequest(method, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //event = response;
                        try{
                            Log.i("event response", response.toString());
                            JSONArray events = response.getJSONArray("events");
                            if(mClusterManager.getMarkerCollection().getMarkers().size() != events.length()){
                                mClusterManager.clearItems();
                                addMarkers(events);
                            }
                            try{
                                Double lat, lng;
                                LatLng startLocation;

                                lat = Double.parseDouble((String)getIntent().getExtras().get(EXTRA_EVENT_LAT));
                                lng = Double.parseDouble((String)getIntent().getExtras().get(EXTRA_EVENT_LNG));
                                startLocation = new LatLng(lat, lng);

                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 17f));
                                Log.i("LATLNG FOR EVENT", startLocation.toString());
                            }
                            catch(Exception e){
                                Log.i("SOME LATLNG", e.toString());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLatLng, 17f));
                            }

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
        Volley.newRequestQueue(this).add(objRequest);

    }


}
