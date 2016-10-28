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

public class LoginActivity extends Activity {
    private CallbackManager callbackManager;

    private LoginButton loginButton;
    ProfilePictureView profilePic;
    TextView info;

    JSONObject likes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        callbackManager = CallbackManager.Factory.create();

        info = (TextView) findViewById(R.id.info);

        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setReadPermissions("public_profile");
        //loginButton.setReadPermissions(Arrays.asList("user_likes"));
        LoginManager.getInstance().logInWithReadPermissions(
                LoginActivity.this,
                Arrays.asList("user_likes"));

        profilePic = (ProfilePictureView) findViewById(R.id.picture);

        if(AccessToken.getCurrentAccessToken() != null){
            RequestData(AccessToken.getCurrentAccessToken());
        }
        FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {

            TextView info = (TextView) findViewById(R.id.info);

            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                Toast.makeText(getApplicationContext(), "Logging in...", Toast.LENGTH_SHORT).show();
                RequestData(accessToken);

            }

            @Override
            public void onCancel() {
                info.setText("failed");
            }

            @Override
            public void onError(FacebookException e) {
                info.setText(e.toString());
            }
        };

        loginButton.registerCallback(callbackManager, callback);


    }

    @Override
    protected void onResume() {
        super.onResume();
        //Facebook login

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
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        //Facebook login
        callbackManager.onActivityResult(requestCode, responseCode, intent);

    }

    private void nextActivity(){

    }

    public void RequestData(final AccessToken accessToken){
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object,GraphResponse response) {

                JSONObject json = response.getJSONObject();
                try {
                    if(json != null){
                        String text = "<b>Name :</b> "+json.getString("name")+"<br><br><b>Profile link :</b> "+json.getString("link");
                        info.setText(json.getString("name"));
                        profilePic.setProfileId(json.getString("id"));
                    }
                    else{
                        info.setText("No user information to display");
                    }
                    likes = object.getJSONObject("likes");
                    DisplayLikes();

                } catch (JSONException e) {
                    e.printStackTrace();
                    info.setText(e.toString());
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,picture, likes{name, category}");
        request.setParameters(parameters);
        request.executeAsync();
    }

//    public void requestLikes(AccessToken accessToken, String id){
//        new GraphRequest(
//                AccessToken.getCurrentAccessToken(),
//                "/" + id +" /likes",
//                null,
//                HttpMethod.GET,
//                new GraphRequest.Callback() {
//                    public void onCompleted(GraphResponse response) {
//            /* handle the result */
//                        info.setText(response.toString());
//                    }
//                }
//        ).executeAsync();
//    }
//
    public void DisplayLikes(){
        String like_list = "";
        String like = "";
        List<String> List_file = new ArrayList<String>();
        ListView listView = (ListView)findViewById(R.id.likes_list);
        try {
            JSONArray data = likes.getJSONArray("data");
            for(int i = 0; i < data.length(); i++){
                JSONObject objectIn = data.getJSONObject(i);
                like = objectIn.getString("name");
                List_file.add(like);
                like_list +=", " + like;


            }
            Log.e("LIKES", like_list);
            List_file.add(like_list);
            listView.setAdapter(new ArrayAdapter<String>(LoginActivity.this, android.R.layout.simple_list_item_1,List_file));
        }
        catch (JSONException e) {
            Log.e("MYAPP", "unexpected JSON exception", e);
            // Do something to recover ... or kill the app.
        }

    }
}
