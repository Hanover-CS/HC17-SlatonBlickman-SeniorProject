package edu.hanover.basin.Events.Activities;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import edu.hanover.basin.Events.Objects.EventsAdapter;
import edu.hanover.basin.Map.Fragments.LocationDialog;
import edu.hanover.basin.LoginActivity;
import edu.hanover.basin.Map.Activities.MapsActivity;
import edu.hanover.basin.R;
import edu.hanover.basin.Request.Objects.basinURL;
import edu.hanover.basin.Utils.ArrayUtil;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

/**
 * Activity for displaying both the created and attended events of the user.
 *
 * @author Slaton Blickman
 * @see AppCompatActivity
 */
public class UserEventsActivity extends AppCompatActivity {
    /**
     * Field used for setting and getting the user Facebook ID.
     * Currently unused.
     * TODO: Allow use of IDs other than the current user's to get event information
     */
    public static final String EXTRA_FACEBOOK_ID = "UserFacebookID";

    //instance variables
    private String fb_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_events);

        AccessToken token =  AccessToken.getCurrentAccessToken();
        if(token != null){
            fb_id = token.getUserId();
        }

    }

    @Override
    public void onResume(){
        super.onResume();

        //request the events
        //doing this in onResume allows us to refresh the lists everytime
        getUserEvents();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_lists, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        Intent intent;
        switch (item.getItemId()) {
            case R.id.map_icon:
                //check if the user has granted access to fine_location
                if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                    return true;
                }

                LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
                boolean enabled = service
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);

                // check if enabled and if not send user to the GSP settings
                // Better solution would be to display a dialog and suggesting to
                // go to the settings
                if (!enabled) {
                    DialogFragment dialogFragment = new LocationDialog();
                    dialogFragment.show(getFragmentManager(), "locationCheck");

                    return true;
                }

                else{
                    intent = new Intent(UserEventsActivity.this, MapsActivity.class);
                    startActivity(intent);

                    return true;
                }
            case R.id.home_icon:
                intent = new Intent(UserEventsActivity.this, LoginActivity.class);
                intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getUserEvents(){
        // Request a string response
        basinURL url = new basinURL();
        HashMap<String, String> params = new HashMap<>();

        params.put("facebook_id", "true");
        url.getUserEventsURL(fb_id, params);

        Log.i("BASIN URL", url.toString());
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url.toString(), null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            JSONObject events = response.getJSONObject("events");
                            Log.i("user events", events.toString());
                            //EventList created_events = new EventList(events.getJSONArray("created"));
                            ArrayList<JSONObject> created_events = ArrayUtil.toArrayList(events.getJSONArray("created"));
                            setAdapters(created_events, R.id.created_list);

                            //EventList attended_events = new EventList(events.getJSONArray("attending"));
                            ArrayList<JSONObject> attended_events = ArrayUtil.toArrayList(events.getJSONArray("attending"));
                            setAdapters(attended_events, R.id.attended_list);

                        }
                        catch(JSONException e){
                            Log.e("userEventsActivityError", e.toString());
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                // Error handling
                Log.e("Volley error", "Something went wrong!");
                error.printStackTrace();

            }

        });

        // Add the request to the queue
        Volley.newRequestQueue(this).add(stringRequest);
    }

    //sets the arrayAdapters for the givn listViews
    private void setAdapters(ArrayList<JSONObject> events, int listViewId){
        // Create the adapter to convert the array to views
        EventsAdapter adapter = new EventsAdapter(this, events);
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(listViewId);
        listView.setAdapter(adapter);
    }
}
