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
 * Created by Slaton on 3/11/2017.
 */

public class UsersAdapter extends ArrayAdapter<JSONObject> {

    public UsersAdapter(Context context, ArrayList<JSONObject> users) {
        super(context, 0, users);
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
        ProfilePictureView picture = (ProfilePictureView) convertView.findViewById(R.id.picture);
        TextView attendee_name = (TextView) convertView.findViewById(R.id.coordinator);
        // Populate the data into the template view using the data object
        try {
            picture.setProfileId(event.getString("facebook_created_by"));
            picture.setPresetSize(ProfilePictureView.SMALL);

            String attendee = event.getString("fname") + " " + event.getString("lname");
            attendee_name.setText(attendee);

        }
        catch(JSONException e){
            Log.e("EventsAdapter error", e.toString());
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                try {
                   // Log.e("event", event.toString());
                    intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, event.getString("event_id"));
                }
                catch(JSONException e){
                    Log.e("JSON EXCEPTION", e.toString());
                }
                v.getContext().startActivity(intent);
            }
        });
        return convertView;
    }
}
