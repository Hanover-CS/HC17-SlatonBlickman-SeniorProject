package edu.hanover.basin;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import static edu.hanover.basin.EventDetailsActivity.EXTRA_EVENT_ID;

public class EventCreationActivity extends Activity {

    public static final String EXTRA_EVENT_LATLNG= "EventLatLng";
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creation);

        location = (String)getIntent().getExtras().get(EXTRA_EVENT_LATLNG);
        //if need to convert from string to LatLng
//        String[] location = ((String)getIntent().getExtras().get(EXTRA_EVENT_LATLNG)).split(",");
//        double lat = Double.parseDouble(location[0]);
//        double lng = Double.parseDouble(location[1]);
//        LatLng latLng = new LatLng(lat, lng);

    }
}
