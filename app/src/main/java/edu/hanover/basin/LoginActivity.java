package edu.hanover.basin;

import android.app.Activity;
import android.os.Bundle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//insert javadoc stuff
public class LoginActivity extends Activity {
    private final String FACEBOOK_ID_EXTRA = "";

    private CallbackManager callbackManager;

    private LoginButton loginButton;
    ProfilePictureView profilePic;
    TextView info;
    TextView age;
    TextView location;
    private User current;

    JSONObject likes;

    private List<String> List_file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        callbackManager = CallbackManager.Factory.create();

        List_file = new ArrayList<String>();

        info = (TextView) findViewById(R.id.info);
        age = (TextView) findViewById(R.id.age);
        location = (TextView) findViewById(R.id.location);

        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile, user_birthday, user_likes"));


        profilePic = (ProfilePictureView) findViewById(R.id.picture);

        if(AccessToken.getCurrentAccessToken() != null){
            current = new User(AccessToken.getCurrentAccessToken());
            Log.e("ACCESS TOKEN:", "NOT NULL");
        }
        FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {

            TextView info = (TextView) findViewById(R.id.info);

            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                Toast.makeText(getApplicationContext(), "Logging in...", Toast.LENGTH_SHORT).show();
                current = new User(accessToken);


            }

            @Override
            public void onCancel() {
                info.setText("failed");
                current = null;
            }

            @Override
            public void onError(FacebookException e) {
                info.setText(e.toString());
                current = null;
            }
        };

        loginButton.registerCallback(callbackManager, callback);
        displayInfo();

    }

    private void displayInfo(){
        if (current != null){
            info.setText(current.getName());
            age.setText(current.getBirthday());
            profilePic.setProfileId(current.getFacebookID());
            displayLikes();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Facebook login
        displayInfo();
        nextActivity();

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

    private void nextActivity(){

    }
    private void displayLikes(){

        ListView listView = (ListView)findViewById(R.id.likes_list);
        if (current.getFacebookLikes() != null) {
            listView.setAdapter(new ArrayAdapter<String>(LoginActivity.this, android.R.layout.simple_list_item_1, current.getFacebookLikes()));
        }
        else{
            Log.e("LISTVIEW ERROR: ", "NO LIKES TO DISPLAY");

        }
    }

}
