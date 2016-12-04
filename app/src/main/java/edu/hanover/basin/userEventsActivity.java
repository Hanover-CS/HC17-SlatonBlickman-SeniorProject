package edu.hanover.basin;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class userEventsActivity extends Activity {
    public static final String EXTRA_FACEBOOK_ID = "UserFacebookID";
    String fb_id;
    JSONObject events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_events);
        AccessToken token =  AccessToken.getCurrentAccessToken();
        if(token != null){
            fb_id = token.getUserId();
        }

        basinURL burl = new basinURL();
        HashMap<String, String> params = new HashMap<>();
        params.put("facebook_id", "true");
        burl.getUserEventsURL(fb_id, params);
        Log.i("BASIN URL", burl.toString());
        request(burl.toString());


        // Attach the adapter to a ListView
    }

    private void request(String url){
        // Request a string response
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            events = response.getJSONObject("events");
                            JSONArray created = events.getJSONArray("created");

                            Log.e("created", created.toString());

                            EventList created_events = new EventList(events.getJSONArray("created"));
                            ArrayList<JSONObject> created_arrayList = created_events.toArrayList();
                            // Create the adapter to convert the array to views
                            EventsAdapter adapter = new EventsAdapter(getApplicationContext(), created_arrayList);
                            // Attach the adapter to a ListView
                            ListView listView = (ListView) findViewById(R.id.created_list);
                            listView.setAdapter(adapter);

                        }
                        catch(JSONException e){
                            Log.e("userEvents error", e.toString());
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
}
