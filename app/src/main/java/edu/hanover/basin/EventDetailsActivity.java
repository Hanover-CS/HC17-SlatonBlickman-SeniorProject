package edu.hanover.basin;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONException;
import org.json.JSONObject;

public class EventDetailsActivity extends Activity {
    public static final String EXTRA_EVENT_ID = "EventID";
    private String event_id;
    private JSONObject event;
    private TextView title;
    private ProfilePictureView picture;
    private TextView coordinator;
    private TextView description;
    private ListView attendees;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        event_id = (String)getIntent().getExtras().get(EXTRA_EVENT_ID);
        title = (TextView)findViewById(R.id.title);
        picture = (ProfilePictureView)findViewById(R.id.picture);
        coordinator = (TextView)findViewById(R.id.coordinator);
        Log.e("why no event id", event_id);
        basinURL url = new basinURL();
        url.getEventURL(event_id);
        request(Request.Method.GET, url.toString());
    }

    private void request(int method, String url){
        // Request a string response
        JsonObjectRequest stringRequest = new JsonObjectRequest(method, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        event = response;
                        try{
                            Log.i("event response", event.toString());
                            title.setText(event.getString("title"));
                            picture.setProfileId(event.getString("facebook_created_by"));
                            coordinator.setText(event.getString("fname") + event.getString("lname"));
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

    //take body and parameters as well

}
