package edu.hanover.basin;

import android.Manifest;
import android.app.DialogFragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import android.content.Intent;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;

import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;


import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.Arrays;

import edu.hanover.basin.Events.Activities.UserEventsActivity;
import edu.hanover.basin.Map.Activities.MapsActivity;
import edu.hanover.basin.Map.Fragments.LocationDialog;
import edu.hanover.basin.Request.Objects.basinURL;
import edu.hanover.basin.Users.Activities.ProfileActivity;
import edu.hanover.basin.Users.Objects.User;

/**
 * Activity for logging into Facebook and basin
 *
 * This activity implements the Facebook login button.
 * It will check for an existing basin user with the given Facebook ID.
 * A POST to insert the user will be made if the GET returns a 404.
 *
 * @author Slaton Blickman
 * @see AppCompatActivity
 * @see User
 */
public class LoginActivity extends AppCompatActivity {

    //Facebook variables
    private CallbackManager callbackManager;
    private AccessToken accessToken;
    private AccessTokenTracker accessTokenTracker;

    //Layout and View variables
    private RelativeLayout loadingPanel, welcomePanel;
    private ScrollView infoContainer;
    private MenuItem profile_icon, map_icon, lists_icon;
    private LoginButton loginButton;
    private ProfilePictureView profilePic;
    private TextView info, greeting;

    //Object variables
    private User current;

    /**
     * This overrides the default onCreate method to do a number of things such as:
     * Initialize the Facebook SDK, register the call back methods for the login button,
     * handling AccessToken changes, and show the loading icon
     * @param savedInstanceState bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        //Log keyhash for the application
        //Keyhash is necessary for Facebook to keep track of application
//        try {
//            PackageInfo info = getPackageManager().getPackageInfo("edu.hanover.basin", PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("YourKeyHash :", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//                System.out.println("YourKeyHash: "+ Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        }
//        catch (Exception e) {
//            Log.e("Key hash exception", e.toString());
//        }

        loadingPanel = (RelativeLayout)findViewById(R.id.loadingPanel);
        welcomePanel = (RelativeLayout)findViewById(R.id.welcomePanel);
        infoContainer = (ScrollView)findViewById(R.id.infoContainer);
        info = (TextView) findViewById(R.id.info);
        greeting = (TextView)findViewById(R.id.greeting);
        profilePic = (ProfilePictureView) findViewById(R.id.picture);
        loginButton = (LoginButton)findViewById(R.id.login_button);

        loadingPanel.setVisibility(View.VISIBLE);

        callbackManager = CallbackManager.Factory.create();
        //Keep track of hte current user; if the accesstoken changes then hte user has changed but probably logged out
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                accessToken = currentAccessToken;

                if (currentAccessToken == null) {
                    //user is no longer logged in
                    //restart the activity
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }

            }
        };
        // If the access token is available already assign it.
        accessToken = AccessToken.getCurrentAccessToken();

        //if the user is logged in, get the current user from Facebook
        if(accessToken != null){
            (new GetCurrentUser()).execute(accessToken);
            Log.i("ACCESS TOKEN:", "NOT NULL");
        }
        else{
            loadingPanel.setVisibility(View.GONE);
        }

        FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                info.setText(getResources().getString(R.string.login_wait));
                loadingPanel.setVisibility(View.VISIBLE);
                accessToken = loginResult.getAccessToken();

                //user has sucessfully logged in; get user information from Facebook
                (new GetCurrentUser()).execute(accessToken);

            }

            @Override
            public void onCancel() {
                info.setText(getResources().getString(R.string.login_failed));
                current = null;
            }

            @Override
            public void onError(FacebookException e) {
                Log.e("Login Error", (e.toString()));
                current = null;
            }
        };

        //set Facebook permisions to ask for when the user clicks the loginbutton
        loginButton.setReadPermissions(Arrays.asList("public_profile, user_birthday, user_likes, user_location"));

        //use defined callback to handle the result from Facebook Login
        loginButton.registerCallback(callbackManager, callback);
    }

    /**
     * Stop access token tracking when activity is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * This can get extra data from the result of the login if needed.
     * Currently does nothing but register it with the callBackManager and call its super method.
     * @param requestCode int
     * @param responseCode int
     * @param data Intent
     */
    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) {
        super.onActivityResult(requestCode, responseCode, data);
        //Facebook login
        //Bundle d = data.getExtras();
        callbackManager.onActivityResult(requestCode, responseCode, data);
    }

    /**
     * Inflates the menu to use menu_main.xml and saves references to menu items for later
     * @param menu menu to inflate
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        profile_icon = menu.findItem(R.id.profile_icon);
        lists_icon = menu.findItem(R.id.lists_icon);
        map_icon = menu.findItem(R.id.map_icon);

        return true;
    }

    /**
     * Handles what to do on menu item clicks.
     * Currently does the following:
     * (profile_icon) Starts ProfileActivity for the current user.
     * (lists_icon) Starts UserEventsActivity for the current user.
     * (map_icon) Checks permissions for FINE_LOCATION and GPS enabled before starting MapsActivity
     * @param item menu item to check for
     * @return boolean for success
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        Intent intent;
        switch (item.getItemId()) {
            case R.id.profile_icon:
                //Go to current user's profile
                intent = new Intent(LoginActivity.this, ProfileActivity.class);
                intent.putExtra(ProfileActivity.EXTRA_FACEBOOK_ID, current.getFacebookID());
                startActivity(intent);
                return true;
            case R.id.lists_icon:
                //Go to user's event lists
                intent = new Intent(LoginActivity.this, UserEventsActivity.class);
                intent.putExtra(UserEventsActivity.EXTRA_FACEBOOK_ID, current.getFacebookID());
                startActivity(intent);
                return true;
            case R.id.map_icon:
                //check if the user has granted access to fine_location
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    return true;
                }

                LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
                boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
                // if access granted, go to
                // if not display a dialog and suggest to go to the settings
                if (!enabled) {
                    DialogFragment dialogFragment = new LocationDialog();
                    dialogFragment.show(getFragmentManager(), "locationCheck");
                    return true;
                }
                else{
                    intent = new Intent(LoginActivity.this, MapsActivity.class);
                    startActivity(intent);
                    return true;
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateViewVisibility(){
        info.setText(getResources().getString(R.string.about));

        profile_icon.setVisible(true);
        map_icon.setVisible(true);
        lists_icon.setVisible(true);
        infoContainer.setVisibility(View.VISIBLE);
        welcomePanel.setVisibility(View.VISIBLE);
        loadingPanel.setVisibility(View.GONE);
        Log.i("UI UPDATED:", "SUCCESS");
    }

    //private method to get the basinUser given a few parameters
    //we can use to try and get the user from basin web or add them if a 404 is received
    private void getBasinUser( int method, JSONObject body, final int tries ){
        basinURL url = new basinURL();

        //Change the url to use an ID if GET, else (on POST) use a URL with no specific id
        if(method == Request.Method.GET){
            url.getUserURL(current.getFacebookID(), "true");
        }
        else{
            url.getUserURL("");
        }

        if(tries < 3) {
            JsonObjectRequest jsonRequest = new JsonObjectRequest(method, url.toString(), body,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            // Result handling
                            Log.i("Volley Response", response.toString());
                            //Update views to show and hide loading stuff
                            updateViewVisibility();
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                    // Error handling
                    //Give the request 3 tries to try to insert the user into the database
                    JSONObject body = new JSONObject();
                    String[] names = current.getName().split(" ");

                    try {
                        body.put("fname", names[0]);
                        body.put("lname", names[1]);
                        body.put("facebook_id", current.getFacebookID());
                    } catch (JSONException e2) {
                        Log.e("JSON EXCEPTION", e2.toString());
                    }

                    getBasinUser(Request.Method.POST, body, tries + 1);
                    //Log.e("Volley error", Log.getStackTraceString(error));
                    error.printStackTrace();

                }

            });

            // Add the request to the queue
            Volley.newRequestQueue(this).add(jsonRequest);
        }
        else{
            info.setText(getResources().getString(R.string.login_error));
            loadingPanel.setVisibility(View.GONE);
        }
    }

    //Private class for handling Facebook Graph Responses as an AsyncTask
    private class GetCurrentUser extends AsyncTask<AccessToken, Void, String>{

        @Override
        protected String doInBackground(AccessToken... params){
            //construct a new empty user Object with the given id from the given accessToken
            current = new User(params[0].getUserId());
            //This activity does not need likes
            current.doLikes(false);
            //Begin requesting data for User
            current.startRequest();
            return "success";
        }

        @Override
        protected void onPostExecute(String results){
            //Set welcome message
            greeting.setText("Welcome, " + current.getName() + "!");
            profilePic.setProfileId(current.getFacebookID());

            //if Facebook login is successful, get the user from basinWeb
            getBasinUser(Request.Method.GET, null, 0);
        }

    }



}
