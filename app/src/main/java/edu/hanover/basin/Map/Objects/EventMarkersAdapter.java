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
 * A class for constructing a EventMarkerAdapter that will be used for display of ListView items that are EventMarkers
 * @author Slaton Blickman
 * @see ArrayAdapter
 */
public class EventMarkersAdapter extends ArrayAdapter<EventMarker> {

    /**
     * basic constructor for class that just calls the super method of construction
     * @param context the application context at the time of constructor call.
     * @param events the EventMarkers to be processed.
     */
    public EventMarkersAdapter(Context context, ArrayList<EventMarker> events) {
        super(context, 0, events);
    }

    /**
     * Overrides the default getView to display EventMarker information.
     * Uses the same layout as EventsAdapter but hides the coordinator information
     * and changes the TextColors to work with the dialogue it should be shown in.
     * Sets the onClickListener for the item to start EventDetailsActivity
     *
     * @param position the position of the item in the list
     * @param convertView the view to be inflated as an item_event
     * @param parent ViewGroup to use in inflater
     * @return View
     */
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
