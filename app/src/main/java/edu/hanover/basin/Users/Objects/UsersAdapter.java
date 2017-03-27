package edu.hanover.basin.Users.Objects;

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

import edu.hanover.basin.R;
import edu.hanover.basin.Users.Activities.ProfileActivity;

/**
 * A class for constructing a UsersAdapter that will be used for display of ListView items that are JSONObjects representing users
 * @author Slaton Blickman
 * @see ArrayAdapter
 */
public class UsersAdapter extends ArrayAdapter<JSONObject> {

    /**
     * basic constructor for class that just calls the super method of construction
     * @param context the application context at the time of constructor call.
     * @param users the JSONObjects to be processed.
     */
    public UsersAdapter(Context context, ArrayList<JSONObject> users) {
        super(context, 0, users);
    }

    /**
     * Overrides the default getView to display user information.
     * List items show Facebook profile picture of the person and their name.
     * Sets the onClickListener for the items to start ProfileActivity
     *
     * @param position the position of the item in the list
     * @param convertView the view to be inflated as an item_event
     * @param parent ViewGroup to use in inflater
     * @return View
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final JSONObject user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_user, parent, false);
        }
        // Lookup view for data population
        ProfilePictureView picture = (ProfilePictureView) convertView.findViewById(R.id.attendee_picture);
        TextView attendee_name = (TextView) convertView.findViewById(R.id.attendee_name);
        // Populate the data into the template view using the data object
        try {
            picture.setProfileId(user.getString("facebook_id"));
            picture.setPresetSize(ProfilePictureView.SMALL);

            String attendee = user.getString("fname") + " " + user.getString("lname");
            attendee_name.setText(attendee);

        }
        catch(JSONException e){
            Log.e("UsersAdapter error", e.toString());
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                try {
                   // Log.e("event", event.toString());
                    intent.putExtra(ProfileActivity.EXTRA_FACEBOOK_ID, user.getString("facebook_id"));
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
