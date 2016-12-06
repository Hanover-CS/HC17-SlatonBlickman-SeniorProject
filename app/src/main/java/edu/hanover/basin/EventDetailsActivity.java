package edu.hanover.basin;

import android.app.Activity;
import android.os.Bundle;

public class EventDetailsActivity extends Activity {
    public static final String EXTRA_EVENT_ID = "EventID";
     private int event_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
    }
}
