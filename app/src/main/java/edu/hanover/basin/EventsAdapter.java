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
            date_time.setText("some time here");
            // Return the completed view to render on screen

        }
        catch(JSONException e){
            Log.e("EventsAdapter error", e.toString());
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                try {
                    Log.e("event", event.toString());
                    Intent intent = new Intent(v.getContext(), EventDetailsActivity.class);
                    intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, event.getString("_id"));
                    v.getContext().startActivity(intent);
                }
                catch(JSONException e){
                    Log.e("JSON EXCEPTION", e.toString());
                }
            }
        });
        return convertView;
    }
}