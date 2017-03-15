package edu.hanover.basin;

import android.app.Activity;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static edu.hanover.basin.EventDetailsActivity.EXTRA_EVENT_ID;
import static edu.hanover.basin.R.id.datePicker;

public class EventCreationActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_LAT = "EventLat";
    public static final String EXTRA_EVENT_LNG = "EventLng";
    public static final String EXTRA_UPDATING = "EventUpdate";
    public static final String EXTRA_TITLE = "EventTitle";
    public static final String EXTRA_DESCRIPTION = "EventDesc";
    public static final String EXTRA_TIME = "EventTime";
    public static final String EXTRA_DATE = "EventDate";
    public static final String EXTRA_EVENT_ID = "EventID";

    private boolean updating;
    private String location;
    private double lat;
    private double lng;
    private int requestMethod;
    private basinURL url;
    private String eventID;

    private EditText title;
    private EditText description;
    private EditText Time;
    private DatePicker date;
    private String facebook_id;
    private Button create;

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
        date = (DatePicker)findViewById(R.id.datePicker);
        create = (Button)findViewById(R.id.create);

        lat = (Double)getIntent().getExtras().get(EXTRA_EVENT_LAT);
        lng = (Double)getIntent().getExtras().get(EXTRA_EVENT_LNG);
        location = String.valueOf(lat) + ", " + String.valueOf(lng);
        updating = (Boolean)getIntent().getExtras().get(EXTRA_UPDATING);

        //description.setText(location);
        url = new basinURL();

        if(updating){
            String editTitle, editDesc, editTime, editDate;

            editDate =(String)getIntent().getExtras().get(EXTRA_DATE);
            editTitle = (String)getIntent().getExtras().get(EXTRA_TITLE);
            editDesc = (String)getIntent().getExtras().get(EXTRA_DESCRIPTION);
            editTime = (String)getIntent().getExtras().get(EXTRA_TIME);
            eventID = (String)getIntent().getExtras().get(EXTRA_EVENT_ID);

            create.setText("SAVE!");
            title.setText(editTitle);
            description.setText(editDesc);
            Time.setText(editTime);

            requestMethod = Request.Method.PUT;
            url.getEventURL(eventID);

        }
        else{
            requestMethod = Request.Method.POST;
            url.getEventURL("");
        }



        //if need to convert from string to LatLng
//        String[] location = ((String)getIntent().getExtras().get(EXTRA_EVENT_LATLNG)).split(",");
//        double lat = Double.parseDouble(location[0]);
//        double lng = Double.parseDouble(location[1]);
//        LatLng latLng = new LatLng(lat, lng);

//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        String dateString = sdf.format(date1);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    public void onClickCreateEvent(View v){
        //regex reference http://www.mkyong.com/regular-expressions/how-to-validate-time-in-24-hours-format-with-regular-expression/
        Pattern pattern;
        Matcher matcher;
        String TIME24HOURS_PATTERN = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
        pattern = Pattern.compile(TIME24HOURS_PATTERN);

        try{
            JSONObject body = new JSONObject();

            body.put("facebook_created_by", facebook_id);
            body.put("title", title.getText());
            body.put("description", description.getText());
            body.put("lat_coord", lat);
            body.put("long_coord", lng);
            //SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMMM d, yy");
            body.put("date", (date.getMonth() + 1) + "-" + date.getDayOfMonth() + "-" + date.getYear());

            matcher = pattern.matcher(Time.getText());

            if(matcher.matches()) {
                body.put("time_start", Time.getText());
                request(requestMethod, url.toString(), body);
            }
            else{
                Toast.makeText(this, "Time is invalid!", Toast.LENGTH_SHORT).show();
            }

        }
        catch(JSONException e){
            Log.e("JSON EXCEPTION", e.toString());
        }

    }

    private void request(int method, String url, JSONObject body){
        // Request a string response
        Log.i("Requesting: ", url);
        Log.i("Body:", body.toString());
        JsonObjectRequest stringRequest = new JsonObjectRequest(method, url, body,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //event = response;
                        try{
                            Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_LONG).show();
                            finish();
                       }

                        catch(Exception e){
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
