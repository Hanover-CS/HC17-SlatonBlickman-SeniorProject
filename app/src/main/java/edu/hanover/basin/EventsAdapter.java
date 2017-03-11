package edu.hanover.basin;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Slaton on 12/4/2016.
 */

public class EventsAdapter extends ArrayAdapter<JSONObject> {

    public EventsAdapter(Context context, ArrayList<JSONObject> events) {
        super(context, 0, events);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final JSONObject event = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_event, parent, false);
        }
        // Lookup view for data population
        TextView title = (TextView) convertView.findViewById(R.id.title);
        ProfilePictureView picture = (ProfilePictureView) convertView.findViewById(R.id.picture);
        TextView coordinator = (TextView) convertView.findViewById(R.id.coordinator);
        TextView date_time = (TextView) convertView.findViewById(R.id.date_time);
        // Populate the data into the template view using the data object
        try {
            title.setText(event.getString("title"));

            picture.setProfileId(event.getString("facebook_created_by"));
            picture.setPresetSize(ProfilePictureView.SMALL);

            String coordinator_name = event.getString("fname") + " " + event.getString("lname");
            coordinator.setText(coordinator_name);

            //date_time.setText(event.getString("time_start"));
            date_time.setText("Time: " + event.getString("time_start") + "\nDate: " + event.getString("date"));
            // Return the completed view to render on screen

        }
        catch(JSONException e){
            Log.e("EventsAdapter error", e.toString());
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                Intent intent = new Intent(v.getContext(), EventDetailsActivity.class);
                try {
                    Log.e("event", event.toString());
                    intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, event.getString("event_id"));
                }
                catch(JSONException e){
                    Log.e("JSON EXCEPTION", e.toString());
                    try{
                        Log.e("event_id not found", "Using different ID field for event");
                        intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, event.getString("_id"));
                    }
                    catch (JSONException e2) {
                        Log.e("JSON EXCEPTION", e2.toString());
                    }
                }
                v.getContext().startActivity(intent);
            }
        });
        return convertView;
    }
}