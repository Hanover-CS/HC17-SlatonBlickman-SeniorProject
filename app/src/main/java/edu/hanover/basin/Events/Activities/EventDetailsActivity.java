package edu.hanover.basin.Events.Activities;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.Profile;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

import edu.hanover.basin.Map.Activities.MapsActivity;
import edu.hanover.basin.Map.Fragments.LocationDialog;
import edu.hanover.basin.R;
import edu.hanover.basin.Request.Objects.basinURL;
import edu.hanover.basin.Users.Activities.ProfileActivity;
import edu.hanover.basin.Users.Objects.UsersAdapter;
import edu.hanover.basin.Utils.ArrayUtil;
import edu.hanover.basin.Utils.ImageUtil;

import static android.content.Intent.FLAG_ACTIVITY_NO_HISTORY;

@SuppressWarnings("ALL")
public class EventDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "EventID";

    private static final String GET_EVENT = "GetEvent";
    private static final String DELETE_EVENT = "DeleteEvent";
    private static final String IS_ATTENDING = "IsAttending";
    private static final String POST_ATTENDING = "PostAttending";
    private static final String GET_ATTENDEES = "GetAttendees";
    private static final String DELETE_ATTENDING = "DeleteAttending";

    private String event_id, facebook_id, lat, lng;
    private JSONObject event;
    private JSONArray attendees;

    private MenuItem menu_checked, menu_edit, menu_delete;
    private RelativeLayout loadingPanel;
    private TextView title, coordinator, time, date, description;
    private ProfilePictureView picture;
    private ImageView event_map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        basinURL url = new basinURL();

        facebook_id = Profile.getCurrentProfile().getId();

        loadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);
        loadingPanel.setVisibility(View.VISIBLE);
        event_map = (ImageView)findViewById(R.id.event_map);
        event_id = (String)getIntent().getExtras().get(EXTRA_EVENT_ID);
        title = (TextView)findViewById(R.id.title);
        picture = (ProfilePictureView)findViewById(R.id.picture);
        coordinator = (TextView)findViewById(R.id.coordinator);
        description = (TextView) findViewById(R.id.description);
        time = (TextView) findViewById(R.id.time);
        date = (TextView) findViewById(R.id.date);

        url.getIsAttendingURL(event_id, facebook_id);
        //request(Request.Method.GET, url.toString(), null, IS_ATTENDING);

    }

    @Override
    protected void onResume(){
        super.onResume();

        basinURL url = new basinURL();

        url.getEventURL(event_id);
        request(Request.Method.GET, url.toString(), null, GET_EVENT);
        url.getIsAttendingURL(event_id, facebook_id);
        request(Request.Method.GET, url.toString(), null, IS_ATTENDING);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);

        //Save items so they can be changed later
        menu_edit = menu.findItem(R.id.edit_icon);
        menu_delete = menu.findItem(R.id.edit_icon);
        menu_checked = menu.findItem(R.id.check_attending_icon);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        Intent intent;
        switch (item.getItemId()) {
            case R.id.check_attending_icon:
                basinURL attendingURL = new basinURL();
                JSONObject body = new JSONObject();
                try{
                    body.put("user_id", Profile.getCurrentProfile().getId());
                }
                catch(JSONException e){
                    //Log.e("", Profile.getCurrentProfile().getId());
                    Log.e("JSON EXCEPTION", e.toString());
                }
                if (!item.isChecked()) {
                    Log.i("IsChecked", "false");
                    attendingURL.getEventAttendeesURL(event_id);
                    //Log.e("USER ID", Profile.getCurrentProfile().getId())
                    request(Request.Method.POST, attendingURL.toString(), body, POST_ATTENDING);
                    item.setIcon(R.drawable.heart_checked_icon);
                    item.setTitle("Attending!");
                    item.setChecked(true);
                }
                else
                {
                    Log.i("IsChecked", "true");
                    attendingURL.getIsAttendingURL(event_id, facebook_id);
                    request(Request.Method.DELETE, attendingURL.toString(),null, DELETE_ATTENDING);
                    item.setIcon(R.drawable.heart_icon);
                    item.setChecked(false);
                }

                return true;
            case R.id.profile_icon:
                try {
                    intent = new Intent(EventDetailsActivity.this, ProfileActivity.class);
                    intent.putExtra(ProfileActivity.EXTRA_FACEBOOK_ID, event.getString("facebook_id"));
                    startActivity(intent);
                }
                catch(JSONException e){
                    e.printStackTrace();
                }
                return true;
            case R.id.marker_icon:
                if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    return true;
                }

                LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
                boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
                // check if enabled and if not send user to the GSP settings
                // Better solution would be to display a dialog and suggesting to
                // go to the settings
                if (!enabled) {
                    DialogFragment dialogFragment = new LocationDialog();
                    dialogFragment.show(getFragmentManager(), "locationCheck");
                    return true;
                }
                else{
                    intent = new Intent(EventDetailsActivity.this, MapsActivity.class);
                    intent.putExtra(MapsActivity.EXTRA_EVENT_LAT, lat);
                    intent.putExtra(MapsActivity.EXTRA_EVENT_LNG, lng);
                    startActivity(intent);
                }
                return true;
            case R.id.delete_icon:
                basinURL deletionUrl = new basinURL();
                deletionUrl.getEventURL(event_id);
                request(Request.Method.DELETE, deletionUrl.toString(), null, DELETE_EVENT);
                return true;
            case R.id.edit_icon:
                try{
                    intent = new Intent(EventDetailsActivity.this, EventCreationActivity.class);
                    intent.putExtra(EventCreationActivity.EXTRA_UPDATING, true);
                    intent.putExtra(EventCreationActivity.EXTRA_EVENT_ID, event.getString("_id"));
                    intent.putExtra(EventCreationActivity.EXTRA_TITLE, event.getString("title"));
                    intent.putExtra(EventCreationActivity.EXTRA_DESCRIPTION, event.getString("description"));
                    intent.putExtra(EventCreationActivity.EXTRA_TIME, event.getString("time_start"));
                    intent.putExtra(EventCreationActivity.EXTRA_DATE, event.getString("date"));
                    intent.putExtra(EventCreationActivity.EXTRA_EVENT_LNG, event.getDouble("long_coord"));
                    intent.putExtra(EventCreationActivity.EXTRA_EVENT_LAT, event.getDouble("lat_coord"));
                    intent.putExtra(EventCreationActivity.EXTRA_ACTIVITY_STARTED, "EventDetails");
                    intent.setFlags(FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                }
                catch(JSONException e){
                    Log.e("JSON EXCEPTION", e.toString());
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void onClickGoToMaps(View v){
        String geoUri = "http://maps.google.com/maps?q=loc:"
                + lat + "," + lng + " (" + title.getText() + ")";
        Intent intent =  new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }

    public void onClickGoToProfile(View v){
        try {
            Intent intent = new Intent(EventDetailsActivity.this, ProfileActivity.class);
            intent.putExtra(ProfileActivity.EXTRA_FACEBOOK_ID, event.getString("facebook_id"));
            startActivity(intent);
        }
        catch(JSONException e){
            e.printStackTrace();
        }
    }

    private void setAdapters(ArrayList<JSONObject> arrayList, int listViewId){
        UsersAdapter adapter = new UsersAdapter(this, arrayList);
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(listViewId);
        listView.setAdapter(adapter);
    }

    private void displayMap(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        int widthRounded = (width - (width % 10))/ 2;
        int heightRounded = (height - (height % 10)) / 2;

        String map_url = "http://maps.google.com/maps/api/staticmap?center="
                + lat + "," + lng
                + "&zoom=15&size=" + widthRounded + "x" + widthRounded
                + "&scale=2&sensor=false&markers=label:Here%7C"
                + lat + "," + lng
                +"&key=AIzaSyD8qiL5jZfvZmJCyNKM1GrfQAe-vgKHauQ"
                ;
        new DownloadImageTask(event_map)
                .execute(map_url);
    }

    private void request(final int method, final String url, JSONObject body, final String type) {
        // Request a string response
        JsonObjectRequest stringRequest = new JsonObjectRequest(method, url, body,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.i("requested url", url);
                            Log.i("response", response.toString());
                            switch (type) {
                                case GET_EVENT:
                                    event = response;
                                    if(event.getString("facebook_created_by").equals(facebook_id)){
                                        Log.i("Coordinator event", "Editing enabled");
                                        menu_checked.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                                        menu_delete.setVisible(true);
                                        menu_edit.setVisible(true);
                                        //supportInvalidateOptionsMenu();
                                    }
                                    else{
                                        menu_edit.setVisible(false);
                                        menu_delete.setVisible(false);
                                    }

                                    title.setText(event.getString("title"));
                                    picture.setProfileId(event.getString("facebook_created_by"));
                                    coordinator.setText(event.getString("fname") + " " + event.getString("lname"));
                                    description.setText(event.getString("description"));
                                    time.setText(event.getString("time_start"));
                                    date.setText(event.getString("date"));
                                    lat = event.getString("lat_coord");
                                    lng = event.getString("long_coord");

                                    displayMap();

                                    basinURL aURL = new basinURL();
                                    aURL.getEventAttendeesURL(event_id);
                                    request(Request.Method.GET, aURL.toString(), null, GET_ATTENDEES);
                                    break;
                                case DELETE_EVENT:
                                    Toast.makeText(EventDetailsActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT)
                                            .show();
                                    finish();
                                    break;
                                case POST_ATTENDING:
                                    break;
                                case DELETE_ATTENDING:
                                    break;
                                case IS_ATTENDING:
                                    if (response.getString("attending").equals("true")) {
                                        menu_checked.setIcon(R.drawable.heart_checked_icon);
                                        menu_checked.setChecked(true);
                                        menu_checked.setTitle(getResources().getString(R.string.attending_y));
                                    }
                                    else {
                                        menu_checked.setIcon(R.drawable.heart_icon);
                                        menu_checked.setChecked(false);
                                        menu_checked.setTitle(getResources().getString(R.string.attending_q));
                                    }
                                    menu_checked.setVisible(true);
                                    break;
                                case GET_ATTENDEES:
                                    attendees = response.getJSONArray("users");
                                    ArrayList<JSONObject> arrayListAttendees = ArrayUtil.toArrayList(attendees);
                                    setAdapters(arrayListAttendees, R.id.users_attending_list);
                                    break;
                                default:
                                    break;
                            }
                            //   description.setText(event.getString("description"));
                        } catch (JSONException e) {
                            Log.e("JSON EXCEPTION", e.toString());
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // Error handling
                Log.e("Volley error", error.toString());
                error.printStackTrace();
            }
        });

        // Add the request to the queue
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private final class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        final ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Log.i("MAP URL", urldisplay);
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.toString());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            //bmImage.setImageBitmap(result);
            bmImage.setImageDrawable(
                    ImageUtil.createRoundedBitmapDrawableWithBorder(getApplicationContext(),result));
            loadingPanel.setVisibility(View.GONE);
            RelativeLayout detailsLayout = (RelativeLayout)findViewById(R.id.activity_event_details);
            detailsLayout.setVisibility(View.VISIBLE);

        }
    }

}
