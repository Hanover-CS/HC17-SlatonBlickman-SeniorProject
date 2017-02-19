package edu.hanover.basin;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.facebook.Profile;
import com.google.android.gms.maps.model.LatLng;

import static edu.hanover.basin.EventDetailsActivity.EXTRA_EVENT_ID;

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
        date = (DatePicker)findViewById(R.id.datePicker);

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

    public void onClickCreate(View v){


    }

}
