package edu.hanover.basin;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONException;
import org.json.JSONObject;

import static com.facebook.AccessToken.getCurrentAccessToken;

public class ProfileActivity extends AppCompatActivity {
    public static final String EXTRA_FACEBOOK_ID = "UserFacebookID";

    private ProfilePictureView profilePic;
    private TextView info;
    private TextView age;
    private TextView location;
    private RelativeLayout loadingPanel;
    MenuItem edit_icon;

    private String id;
    private User current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        id = (String)getIntent().getExtras().get(EXTRA_FACEBOOK_ID);
        loadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);
        info = (TextView) findViewById(R.id.info);
        age = (TextView) findViewById(R.id.age);
        location = (TextView) findViewById(R.id.location);
        profilePic = (ProfilePictureView) findViewById(R.id.picture);

        Log.e("FACEBOOK ID", id);
        (new UpdateProfile()).execute(id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        edit_icon = menu.findItem(R.id.menu_edit);

        if(id.equals(getCurrentAccessToken().getUserId())){
            edit_icon.setVisible(true);
        }

        return true;
    }

    private void basinRequest(){
        basinURL url = new basinURL();
        url.getUserURL(id, "true");
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url.toString(), null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        // Result handling
                        Log.i("Volley Response", response.toString());
                        TextView about = (TextView)findViewById(R.id.about);
                        try{
                            about.setText("About:\n" + response.getString("about"));
                        }
                        catch (JSONException e){
                           Log.e("JSONEXCEPTION!", e.toString());
                        }



                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();

            }

        });

        // Add the request to the queue
        Volley.newRequestQueue(this).add(jsonRequest);
    }
    private class UpdateProfile extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... id){
            current = new User(id[0]);
            current.startRequest();
            basinRequest();
            return "success";
        }

        @Override
        protected void onPostExecute(String results){
            ListView listView = (ListView)findViewById(R.id.likes_list);
            listView.setAdapter(new ArrayAdapter<String>(ProfileActivity.this,
                    android.R.layout.simple_list_item_1,
                    current.getFacebookLikes()));
            info.setText(current.getName());
            age.setText(current.getBirthday());
            profilePic.setProfileId(current.getFacebookID());
            location.setText(current.getLocation());
            loadingPanel.setVisibility(View.GONE);

            Log.e("UI UPDATED:", "SUCCESS");
        }
    }


}
