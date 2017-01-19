package edu.hanover.basin;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class EventCreationActivity extends Activity {

    public static final String EXTRA_EVENT_LATLNG= "EventLatLng";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creation);
    }
}
