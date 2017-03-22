package edu.hanover.basin.Map.Objects;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;

import java.util.ArrayList;

import edu.hanover.basin.Map.Objects.EventMarker;
import edu.hanover.basin.R;

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
        title.setTextColor(Color.BLACK);
        date_time.setTextColor(Color.DKGRAY);
        date_time.setText(event.getSnippet());


        return convertView;
    }
}
