package edu.hanover.basin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

public class EventDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "EventID";

    private static final String GET_EVENT = "GetEvent";
    private static final String IS_ATTENDING = "IsAttending";
    private static final String POST_ATTENDING = "PostAttending";
    private static final String GET_ATTENDEES = "GetAttendees";

    private String event_id, lat, lng;
    private JSONObject event;
    private JSONArray attendees;
    private TextView title, coordinator, time, date, description;
    private ProfilePictureView picture;
    private CheckBox attendingBox;
    private ListView attendeesListView;
    private boolean checkOff;
    private ImageView event_map;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        basinURL url = new basinURL();

        String facebook_id = Profile.getCurrentProfile().getId();

        event_map = (ImageView)findViewById(R.id.event_map);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onClickEdit(View v){
        //Log.e("event is: ", event.toString());
        try{
            Intent intent = new Intent(EventDetailsActivity.this, EventCreationActivity.class);
            intent.putExtra(EventCreationActivity.EXTRA_UPDATING, true);
            intent.putExtra(EventCreationActivity.EXTRA_EVENT_ID, event.getString("_id"));
            intent.putExtra(EventCreationActivity.EXTRA_TITLE, event.getString("title"));
            intent.putExtra(EventCreationActivity.EXTRA_DESCRIPTION, event.getString("description"));
            intent.putExtra(EventCreationActivity.EXTRA_TIME, event.getString("time_start"));
            intent.putExtra(EventCreationActivity.EXTRA_DATE, event.getString("date"));
            intent.putExtra(EventCreationActivity.EXTRA_EVENT_LNG, event.getDouble("long_coord"));
            intent.putExtra(EventCreationActivity.EXTRA_EVENT_LAT, event.getDouble("lat_coord"));
            startActivity(intent);
        }
        catch(JSONException e){
            Log.e("JSON EXCEPTION", e.toString());
        }
    }

    public void onClickGoToMaps(View v){
        Intent intent = new Intent(EventDetailsActivity.this, MapsActivity.class);
        intent.putExtra(MapsActivity.EXTRA_EVENT_LAT, lat);
        intent.putExtra(MapsActivity.EXTRA_EVENT_LNG, lng);
        startActivity(intent);
    }

    public void onClickGoToProfile(View v){
        try {
            Intent intent = new Intent(EventDetailsActivity.this, ProfileActivity.class);
            intent.putExtra(ProfileActivity.EXTRA_FACEBOOK_ID, event.getString("facebook_id"));
            startActivity(intent);
        }
        catch(JSONException e){
            e.printStackTrace();
        }
    }

    private void setAdapters(ArrayList<JSONObject> arrayList, int listViewId){
        UsersAdapter adapter = new UsersAdapter(this, arrayList);
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(listViewId);
        listView.setAdapter(adapter);
    }

    private void displayMap(){
        String map_url = "http://maps.google.com/maps/api/staticmap?center="
                + lat + "," + lng
                + "&zoom=15&size=200x200&scale=2&sensor=false&markers=label:Here%7C"
                + lat + "," + lng
                +"&key=AIzaSyD8qiL5jZfvZmJCyNKM1GrfQAe-vgKHauQ"
                ;
        new DownloadImageTask(event_map)
                .execute(map_url);
    }

    private void request(final int method, final String url, JSONObject body, final String type) {
        // Request a string response
        JsonObjectRequest stringRequest = new JsonObjectRequest(method, url, body,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.i("requested url", url);
                            switch (type) {
                                case GET_EVENT://Log.i("event response", event.toString());
                                    event = response;
                                    Log.i("event response", event.toString());
                                    title.setText(event.getString("title"));
                                    picture.setProfileId(event.getString("facebook_created_by"));
                                    coordinator.setText(event.getString("fname") + " " + event.getString("lname"));
                                    description.setText(event.getString("description"));
                                    time.setText(event.getString("time_start"));
                                    date.setText(event.getString("date"));
                                    lat = event.getString("lat_coord");
                                    lng = event.getString("long_coord");

                                    displayMap();

                                    basinURL aURL = new basinURL();
                                    aURL.getEventAttendeesURL(event_id);
                                    request(Request.Method.GET, aURL.toString(), null, GET_ATTENDEES);
                                    break;
                                case IS_ATTENDING:
                                    if (response.getString("attending") == "true") {
                                        attendingBox.setChecked(true);
                                    } else {
                                        attendingBox.setChecked(false);
                                    }
                                    break;
                                case GET_ATTENDEES:
                                    attendees = response.getJSONArray("users");
                                    ArrayList<JSONObject> arrayListAttendees = ArrayUtil.toArrayList(attendees);
                                    setAdapters(arrayListAttendees, R.id.users_attending_list);
                                    break;
                                default:
                                    break;
                            }
                            //   description.setText(event.getString("description"));
                        } catch (JSONException e) {
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

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Log.v("MAP URL", urldisplay);
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.toString());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }


}
