package edu.hanover.basin;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.Profile;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONException;
import org.json.JSONObject;

public class EventDetailsActivity extends Activity {
    public static final String EXTRA_EVENT_ID = "EventID";
    public static final String GET_EVENT = "GetEvent";
    public static final String IS_ATTENDING = "IsAttending";
    public static final String POST_ATTENDING = "PostAttending";

    private String event_id;
    private JSONObject event;
    private TextView title, coordinator, time, date, description;
    private ProfilePictureView picture;
    private CheckBox attendingBox;
    private ListView attendees;
    private boolean checkOff;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        basinURL url = new basinURL();

        String facebook_id = Profile.getCurrentProfile().getId();

        event_id = (String)getIntent().getExtras().get(EXTRA_EVENT_ID);
        title = (TextView)findViewById(R.id.title);
        picture = (ProfilePictureView)findViewById(R.id.picture);
        coordinator = (TextView)findViewById(R.id.coordinator);
        description = (TextView) findViewById(R.id.description);
        time = (TextView) findViewById(R.id.time);
        date = (TextView) findViewById(R.id.date);
        attendingBox = (CheckBox)findViewById(R.id.attending);

        url.getIsAttendingURL(event_id, facebook_id);
        request(Request.Method.GET, url.toString(), null, IS_ATTENDING);

        attendingBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    basinURL attendingURL = new basinURL();
                    attendingURL.getEventAttendeesURL(event_id);
                    JSONObject body = new JSONObject();
                    try{
                        body.put("user_id", Profile.getCurrentProfile().getId());
                        //Log.e("USER ID", Profile.getCurrentProfile().getId())
                        request(Request.Method.POST, attendingURL.toString(), body, POST_ATTENDING);
                    }
                    catch(JSONException e){
                        //Log.e("", Profile.getCurrentProfile().getId());
                        Log.e("JSON EXCEPTION", e.toString());
                    }
                }

            }
        });

        //Log.e("why no event id", event_id);
        url.getEventURL(event_id);
        request(Request.Method.GET, url.toString(), null, GET_EVENT);
    }

    private void request(final int method, final String url, JSONObject body, final String type){
        // Request a string response
        JsonObjectRequest stringRequest = new JsonObjectRequest(method, url, body,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        event = response;
                        try{
                            Log.i("requested url", url);
                            Log.i("event response", event.toString());
                            switch (type){
                                case GET_EVENT://Log.i("event response", event.toString());
                                    title.setText(event.getString("title"));
                                    picture.setProfileId(event.getString("facebook_created_by"));
                                    coordinator.setText("Coordinator: " + event.getString("fname") + " " + event.getString("lname"));
                                    description.setText("Description:\n" + event.getString("description"));
                                    time.setText("Time: " + event.getString("time_start"));
                                    date.setText("Date: " + event.getString("date"));
                                    break;
                                case IS_ATTENDING:
                                    if(event.getString("attending") == "true"){
                                        attendingBox.setChecked(true);
                                    }
                                    else{
                                        attendingBox.setChecked(false);
                                    }
                                    break;
                                case POST_ATTENDING:
                                    break;
                            }
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
