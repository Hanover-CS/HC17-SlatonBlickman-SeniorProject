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

//insert javadoc stuff
public class LoginActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private AccessToken accessToken;
    private AccessTokenTracker accessTokenTracker;

    private RelativeLayout loadingPanel, welcomePanel;
    private LoginButton loginButton;
    private ProfilePictureView profilePic;
    private TextView info, greeting;

    private User current;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        loadingPanel = (RelativeLayout)findViewById(R.id.loadingPanel);
        welcomePanel = (RelativeLayout)findViewById(R.id.welcomePanel);
        loadingPanel.setVisibility(View.VISIBLE);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }


        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "edu.hanover.basin",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("YourKeyHash :", Base64.encodeToString(md.digest(), Base64.DEFAULT));
                System.out.println("YourKeyHash: "+ Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (Exception e) {

        }

        callbackManager = CallbackManager.Factory.create();

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.

            }
        };
        // If the access token is available already assign it.
        accessToken = AccessToken.getCurrentAccessToken();

        info = (TextView) findViewById(R.id.info);
        greeting = (TextView)findViewById(R.id.greeting);
        profilePic = (ProfilePictureView) findViewById(R.id.picture);
        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile, user_birthday, user_likes, user_location"));


        if(AccessToken.getCurrentAccessToken() != null){
//            current = new User(AccessToken.getCurrentAccessToken());
//            displayInfo();
//            displayLikes();
            (new GetCurrentUser()).execute(AccessToken.getCurrentAccessToken());
            //loadingPanel.setVisibility(View.GONE);
            Log.i("ACCESS TOKEN:", "NOT NULL");
        }
        else{
            loadingPanel.setVisibility(View.GONE);
        }

        FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                info.setText("Logging in...");
                loadingPanel.setVisibility(View.VISIBLE);
                AccessToken accessToken = loginResult.getAccessToken();

                (new GetCurrentUser()).execute(accessToken);



            }

            @Override
            public void onCancel() {
                info.setText("Login Failed");
                current = null;
            }

            @Override
            public void onError(FacebookException e) {
                info.setText(e.toString());
                current = null;
            }
        };

        loginButton.registerCallback(callbackManager, callback);
        //info.setText("PLEASE WORK");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        Intent intent;
        switch (item.getItemId()) {
            case R.id.profile_menu:
                intent = new Intent(LoginActivity.this, ProfileActivity.class);
                intent.putExtra(ProfileActivity.EXTRA_FACEBOOK_ID, current.getFacebookID());
                startActivity(intent);
                return true;
            case R.id.lists_menu:
                intent = new Intent(LoginActivity.this, UserEventsActivity.class);
                intent.putExtra(UserEventsActivity.EXTRA_FACEBOOK_ID, current.getFacebookID());
                startActivity(intent);
                return true;
            case R.id.map_menu:
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
                LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
                boolean enabled = service
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);

                // check if enabled and if not send user to the GSP settings
                // Better solution would be to display a dialog and suggesting to
                // go to the settings
                if (!enabled) {
                    DialogFragment dialogFragment = new LocationDialog();
                    dialogFragment.show(getFragmentManager(), "locationCheck");
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

    @Override
    public void onDestroy() {
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

    protected void onStop() {
        super.onStop();
        //Facebook login

    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) {
        super.onActivityResult(requestCode, responseCode, data);
        //Facebook login
        Bundle d = data.getExtras();
        callbackManager.onActivityResult(requestCode, responseCode, data);

    }


    public void onClickGoToRequests(View v){
        Intent intent = new Intent(LoginActivity.this, BasinWebTestActivity.class);
        intent.putExtra(BasinWebTestActivity.EXTRA_FACEBOOK_ID, current.getFacebookID());
        startActivity(intent);
    }

    private void insertUser(String url){

    }

    private void getUser( int method, JSONObject body, final int tries ){
        basinURL dbUser = new basinURL();
        String url;
        if(method == Request.Method.GET){
            url = dbUser.getUserURL(current.getFacebookID(), "true");
        }
        else{
            url = dbUser.getUserURL("");
        }

        if(body == null || body.length() == 0){
            body = null;
        }
        // Request a string response
        if(tries < 3) {
            JsonObjectRequest jsonRequest = new JsonObjectRequest(method, url, body,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            // Result handling
                            Log.i("Volley Response", response.toString());
                            info.setText(getResources().getString(R.string.about));
                            welcomePanel.setVisibility(View.VISIBLE);
                            loadingPanel.setVisibility(View.GONE);
                            Log.e("UI UPDATED:", "SUCCESS");


                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                    // Error handling
                    JSONObject body = new JSONObject();
                    String[] names = current.getName().split(" ");
                    try {
                        body.put("fname", names[0]);
                        body.put("lname", names[1]);
                        body.put("facebook_id", current.getFacebookID());
                    } catch (JSONException e2) {
                        Log.e("JSON EXCEPTION", e2.toString());
                    }
                    getUser(Request.Method.POST, body, tries + 1);
                    //Log.e("Volley error", Log.getStackTraceString(error));
                    error.printStackTrace();

                }

            });

            // Add the request to the queue
            Volley.newRequestQueue(this).add(jsonRequest);
        }
        else{
            info.setText("Something went wrong during login :(");
            loadingPanel.setVisibility(View.GONE);
        }
    }
    //For graph requests
    private class GetCurrentUser extends AsyncTask<AccessToken, Void, String>{

        @Override
        protected String doInBackground(AccessToken... params){
            current = new User(params[0].getUserId());
            current.doLikes(false);
            current.startRequest();
            return "success";
        }

        @Override
        protected void onPostExecute(String results){
            greeting.setText("Welcome, " + current.getName() + "!");
            profilePic.setProfileId(current.getFacebookID());

            getUser(Request.Method.GET, null, 0);
        }
    }



}
