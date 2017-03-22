package edu.hanover.basin.Events.Activities;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
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
import edu.hanover.basin.Users.Objects.User;
import edu.hanover.basin.Users.Objects.UsersAdapter;
import edu.hanover.basin.Utils.ArrayUtil;

import static android.content.Intent.FLAG_ACTIVITY_NO_HISTORY;

public class EventDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "EventID";

    private static final String GET_EVENT = "GetEvent";
    private static final String DELETE_EVENT = "DeleteEvent";
    private static final String IS_ATTENDING = "IsAttending";
    private static final String POST_ATTENDING = "PostAttending";
    private static final String GET_ATTENDEES = "GetAttendees";
    private static final String DELETE_ATTENDING = "DeleteAttending";

    private String event_id, facebook_id, lat, lng;
    private boolean checkOff, enableEdits, enableAttending;
    private JSONObject event;
    private JSONArray attendees;

    private Menu menu;
    private MenuItem menu_checked, menu_edit, menu_delete;
    private RelativeLayout loadingPanel;
    private TextView title, coordinator, time, date, description;
    private ProfilePictureView picture;
    private ListView attendeesListView;
    private ImageView event_map;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        basinURL url = new basinURL();
        enableAttending = false;

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
                }

                else{
                    intent = new Intent(EventDetailsActivity.this, MapsActivity.class);
                    intent.putExtra(MapsActivity.EXTRA_EVENT_LAT, lat);
                    intent.putExtra(MapsActivity.EXTRA_EVENT_LNG, lng);
                    startActivity(intent);
                    return true;
                }
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
                                case GET_EVENT://Log.i("event response", event.toString());
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
                                    Log.i("event response", event.toString());
                                    title.setText(event.getString("title"));
                                    picture.setProfileId(event.getString("facebook_created_by"));
                                    coordinator.setText(event.getString("fname") + " " + event.getString("lname"));
                                    description.setText(event.getString("description"));
                                    time.setText(event.getString("time_start"));
                                    date.setText(event.getString("date"));
                                    lat = event.getString("lat_coord");
                                    lng = event.getString("long_coord");
                                    //Log.e("Compare ids", event.getString("facebook_created_by") + " vs. " + facebook_id);

                                    displayMap();

                                    basinURL aURL = new basinURL();
                                    aURL.getEventAttendeesURL(event_id);
                                    request(Request.Method.GET, aURL.toString(), null, GET_ATTENDEES);
                                    break;
                                case DELETE_EVENT:
                                    finish();
                                    break;
                                case POST_ATTENDING:
                                    break;
                                case DELETE_ATTENDING:
                                    break;
                                case IS_ATTENDING:
                                    if (response.getString("attending").equals("true")) {
                                        checkOff = true;
                                        menu_checked.setIcon(R.drawable.heart_checked_icon);
                                        menu_checked.setChecked(true);
                                        menu_checked.setTitle(getResources().getString(R.string.attending_y));
                                    } else {
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

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Log.v("MAP URL", urldisplay);
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
            bmImage.setImageDrawable(createRoundedBitmapDrawableWithBorder(result));
            loadingPanel.setVisibility(View.GONE);
            RelativeLayout detailsLayout = (RelativeLayout)findViewById(R.id.activity_event_details);
            detailsLayout.setVisibility(View.VISIBLE);

        }
    }

    //src: https://android--examples.blogspot.com/2015/11/android-how-to-create-circular.html
    private RoundedBitmapDrawable createRoundedBitmapDrawableWithBorder(Bitmap bitmap){
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        int borderWidthHalf = 5; // In pixels
        //Toast.makeText(mContext,""+bitmapWidth+"|"+bitmapHeight,Toast.LENGTH_SHORT).show();

        // Calculate the bitmap radius
        int bitmapRadius = Math.min(bitmapWidth,bitmapHeight)/2;

        int bitmapSquareWidth = Math.min(bitmapWidth,bitmapHeight);
        //Toast.makeText(mContext,""+bitmapMin,Toast.LENGTH_SHORT).show();

        int newBitmapSquareWidth = bitmapSquareWidth+borderWidthHalf;
        //Toast.makeText(mContext,""+newBitmapMin,Toast.LENGTH_SHORT).show();

        /*
            Initializing a new empty bitmap.
            Set the bitmap size from source bitmap
            Also add the border space to new bitmap
        */
        Bitmap roundedBitmap = Bitmap.createBitmap(newBitmapSquareWidth,newBitmapSquareWidth,Bitmap.Config.ARGB_8888);

        /*
            Canvas
                The Canvas class holds the "draw" calls. To draw something, you need 4 basic
                components: A Bitmap to hold the pixels, a Canvas to host the draw calls (writing
                into the bitmap), a drawing primitive (e.g. Rect, Path, text, Bitmap), and a paint
                (to describe the colors and styles for the drawing).

            Canvas(Bitmap bitmap)
                Construct a canvas with the specified bitmap to draw into.
        */
        // Initialize a new Canvas to draw empty bitmap
        Canvas canvas = new Canvas(roundedBitmap);

        /*
            drawColor(int color)
                Fill the entire canvas' bitmap (restricted to the current clip) with the specified
                color, using srcover porterduff mode.
        */
        // Draw a solid color to canvas
        canvas.drawColor(Color.RED);

        // Calculation to draw bitmap at the circular bitmap center position
        int x = borderWidthHalf + bitmapSquareWidth - bitmapWidth;
        int y = borderWidthHalf + bitmapSquareWidth - bitmapHeight;

        /*
            drawBitmap(Bitmap bitmap, float left, float top, Paint paint)
                Draw the specified bitmap, with its top/left corner at (x,y), using the specified
                paint, transformed by the current matrix.
        */
        /*
            Now draw the bitmap to canvas.
            Bitmap will draw its center to circular bitmap center by keeping border spaces
        */
        canvas.drawBitmap(bitmap, x, y, null);

        // Initializing a new Paint instance to draw circular border
        Paint borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidthHalf*2);
        borderPaint.setColor(Color.WHITE);

        /*
            drawCircle(float cx, float cy, float radius, Paint paint)
                Draw the specified circle using the specified paint.
        */
        /*
            Draw the circular border to bitmap.
            Draw the circle at the center of canvas.
         */
        canvas.drawCircle(canvas.getWidth()/2, canvas.getWidth()/2, newBitmapSquareWidth/2, borderPaint);

        /*
            RoundedBitmapDrawable
                A Drawable that wraps a bitmap and can be drawn with rounded corners. You can create
                a RoundedBitmapDrawable from a file path, an input stream, or from a Bitmap object.
        */
        /*
            public static RoundedBitmapDrawable create (Resources res, Bitmap bitmap)
                Returns a new drawable by creating it from a bitmap, setting initial target density
                based on the display metrics of the resources.
        */
        /*
            RoundedBitmapDrawableFactory
                Constructs RoundedBitmapDrawable objects, either from Bitmaps directly, or from
                streams and files.
        */
        // Create a new RoundedBitmapDrawable
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(),roundedBitmap);

        /*
            setCornerRadius(float cornerRadius)
                Sets the corner radius to be applied when drawing the bitmap.
        */
        // Set the corner radius of the bitmap drawable
        roundedBitmapDrawable.setCornerRadius(bitmapRadius);

        /*
            setAntiAlias(boolean aa)
                Enables or disables anti-aliasing for this drawable.
        */
        roundedBitmapDrawable.setAntiAlias(true);

        // Return the RoundedBitmapDrawable
        return roundedBitmapDrawable;
    }


}
