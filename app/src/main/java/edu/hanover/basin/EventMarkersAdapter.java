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

import java.util.ArrayList;

/**
 * Created by Slaton on 3/19/2017.
 */

public class EventMarkersAdapter extends ArrayAdapter<EventMarker> {

    public EventMarkersAdapter(Context context, ArrayList<EventMarker> events) {
        super(context, 0, events);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final EventMarker event = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_event, parent, false);
        }
        // Lookup view for data population
        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView date_time = (TextView) convertView.findViewById(R.id.date_time);
        ProfilePictureView picture = (ProfilePictureView) convertView.findViewById(R.id.picture);
        TextView coordinator = (TextView) convertView.findViewById(R.id.coordinator);
        picture.setVisibility(View.GONE);
        coordinator.setVisibility(View.GONE );
        // Populate the data into the template view using the data object

        Log.i("Event list:", event.toString());
        title.setText(event.getTitle());
        date_time.setText(event.getSnippet());

//            picture.setProfileId(event.getString("facebook_created_by"));
//            picture.setPresetSize(ProfilePictureView.SMALL);
//
//            String coordinator_name = event.getString("fname") + " " + event.getString("lname");
//            coordinator.setText(coordinator_name);

        //date_time.setText(event.getString("time_start"));
        date_time.setText(event.getSnippet());
        // Return the completed view to render on screen


//        convertView.setOnClickListener(new View.OnClickListener() {
//            public void onClick(final View v) {
//                Intent intent = new Intent(v.getContext(), EventDetailsActivity.class);
//                intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, event.getID());
//                v.getContext().startActivity(intent);
//            }
//        });
        return convertView;
    }
}
