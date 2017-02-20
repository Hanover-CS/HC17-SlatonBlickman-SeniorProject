package edu.hanover.basin;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.Profile;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import static edu.hanover.basin.EventDetailsActivity.EXTRA_EVENT_ID;
import static edu.hanover.basin.R.id.datePicker;

public class EventCreationActivity extends Activity {

    public static final String EXTRA_EVENT_LAT = "EventLat";
    public static final String EXTRA_EVENT_LNG = "EventLng";
    private String location;
    private double lat;
    private double lng;
    private EditText title;
    private EditText description;
    private EditText Time;
    private DatePicker date;
    private String facebook_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creation);

        facebook_id = Profile.getCurrentProfile().getId();
        title = (EditText)findViewById(R.id.title);
        title.setText(facebook_id);
        description = (EditText)findViewById(R.id.description);
        Time = (EditText)findViewById(R.id.time);
        //duration
        date = (DatePicker)findViewById(datePicker);

        lat = (Double)getIntent().getExtras().get(EXTRA_EVENT_LAT);
        lng = (Double)getIntent().getExtras().get(EXTRA_EVENT_LNG);
        location = String.valueOf(lat) + ", " + String.valueOf(lng);
        description.setText(location);



        //if need to convert from string to LatLng
//        String[] location = ((String)getIntent().getExtras().get(EXTRA_EVENT_LATLNG)).split(",");
//        double lat = Double.parseDouble(location[0]);
//        double lng = Double.parseDouble(location[1]);
//        LatLng latLng = new LatLng(lat, lng);

//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        String dateString = sdf.format(date1);

    }

    public void onClickCreateEvent(View v){
        JSONObject body = new JSONObject();
        basinURL url = new basinURL();

        try{
            body.put("facebook_created_by", facebook_id);
            body.put("title", title.getText());
            body.put("description", description.getText());
            body.put("lat_coord", lat);
            body.put("long_coord", lng);
            body.put("date", date.getYear() + "-" + (date.getMonth() + 1) + "-" + date.getDayOfMonth());
            body.put("time_start", Time.getText());
        }
        catch(JSONException e){
            Log.e("JSON EXCEPTION", e.toString());
        }

        request(Request.Method.POST, url.postEventURL(), body);


    }

    private void request(int method, String url, JSONObject body){
        // Request a string response
        JsonObjectRequest stringRequest = new JsonObjectRequest(method, url, body,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //event = response;
                        try{
                            Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_LONG);
                            finish();
                            //description.setText(response.toString());
//                            Log.i("event response", event.toString());
//                            title.setText(event.getString("title"));
//                            picture.setProfileId(event.getString("facebook_created_by"));
//                            coordinator.setText(event.getString("fname") + event.getString("lname"));
//                            //   description.setText(event.getString("description"));
                        }
                        catch(Exception e){
                            Log.e("JSON EXCEPTION", response.toString());
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
