package edu.hanover.basin;

import android.app.Activity;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import java.util.Calendar;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

    public static final String EXTRA_ACTIVITY_STARTED = "ActivityStarted";
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
    private EditText time;
    private DatePicker date;
    private String facebook_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creation);

        facebook_id = Profile.getCurrentProfile().getId();
        title = (EditText)findViewById(R.id.title);
        description = (EditText)findViewById(R.id.description);
        time = (EditText)findViewById(R.id.time);
        date = (DatePicker)findViewById(R.id.datePicker);
        date.setMinDate(System.currentTimeMillis() - 10000);

        lat = (Double)getIntent().getExtras().get(EXTRA_EVENT_LAT);
        lng = (Double)getIntent().getExtras().get(EXTRA_EVENT_LNG);
        location = String.valueOf(lat) + ", " + String.valueOf(lng);
        updating = (Boolean)getIntent().getExtras().get(EXTRA_UPDATING);

        url = new basinURL();

    }

    @Override
    protected  void onResume(){
        super.onResume();

        if(updating){
            String editTitle, editDesc, editTime, editDate;
            int year, month, day;

            Calendar cal = Calendar.getInstance();
            cal.set(date.getYear(), date.getMonth() + 1, date.getDayOfMonth());
            cal.add(Calendar.DATE, 120);

            editDate =(String)getIntent().getExtras().get(EXTRA_DATE);
            editTitle = (String)getIntent().getExtras().get(EXTRA_TITLE);
            editDesc = (String)getIntent().getExtras().get(EXTRA_DESCRIPTION);
            editTime = (String)getIntent().getExtras().get(EXTRA_TIME);
            eventID = (String)getIntent().getExtras().get(EXTRA_EVENT_ID);

            year = Integer.parseInt(editDate.substring(5, 8));
            month = Integer.parseInt(editDate.substring(3, 4));
            day = Integer.parseInt(editDate.substring(0,1));

            try{
                date.updateDate(year, month, day);
            }
            catch(Exception e){
                //do nothing
            }

            title.setText(editTitle);
            description.setText(editDesc);
            time.setText(editTime);

            requestMethod = Request.Method.PUT;
            url.getEventURL(eventID);

        }
        else{
            requestMethod = Request.Method.POST;
            url.getEventURL("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_save:
                createEvent();
                return true;
            case R.id.menu_cancel:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean validTime(){
        //regex reference http://www.mkyong.com/regular-expressions/how-to-validate-time-in-24-hours-format-with-regular-expression/
        Pattern pattern;
        Matcher matcher;
        String TIME24HOURS_PATTERN = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
        pattern = Pattern.compile(TIME24HOURS_PATTERN);
        matcher = pattern.matcher(time.getText());
        return matcher.matches();
    }

    public void createEvent(){
        //regex reference http://www.mkyong.com/regular-expressions/how-to-validate-time-in-24-hours-format-with-regular-expression/
        try{
            JSONObject body = new JSONObject();
            body.put("facebook_created_by", facebook_id);
            body.put("title", title.getText());
            body.put("description", description.getText());
            body.put("lat_coord", lat);
            body.put("long_coord", lng);
            //SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMMM d, yy");
            body.put("date", (date.getMonth() + 1) + "-" + date.getDayOfMonth() + "-" + date.getYear());

            if(validTime()) {
                body.put("time_start", time.getText());
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
                            Intent thisIntent = getIntent();
                            String activity;
                            if(thisIntent != null){
                                activity = thisIntent.getExtras().getString(EXTRA_ACTIVITY_STARTED);
                                if(activity.equals("EventDetails")){
                                    finish();
                                }
                                else{

                                    Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(EventCreationActivity.this, MapsActivity.class);
                                    intent.putExtra(MapsActivity.EXTRA_EVENT_LNG, lng);
                                    intent.putExtra(MapsActivity.EXTRA_EVENT_LAT, lat);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }
                            }
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
