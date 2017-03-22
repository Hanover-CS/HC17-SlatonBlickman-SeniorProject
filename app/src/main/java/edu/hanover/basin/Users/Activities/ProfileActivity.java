package edu.hanover.basin.Users.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONException;
import org.json.JSONObject;

import edu.hanover.basin.R;
import edu.hanover.basin.Request.Objects.basinURL;
import edu.hanover.basin.Users.Objects.User;

import static com.facebook.AccessToken.getCurrentAccessToken;

public class ProfileActivity extends AppCompatActivity {
    public static final String EXTRA_FACEBOOK_ID = "UserFacebookID";

    private ProfilePictureView profilePic;
    private TextView info, age, location, about;
    private RelativeLayout loadingPanel;
    private LinearLayout listContainer;
    private MenuItem edit_icon;

    private String id;
    private User current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        id = (String)getIntent().getExtras().get(EXTRA_FACEBOOK_ID);
        loadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);
        listContainer = (LinearLayout) findViewById(R.id.listContainer);
        info = (TextView) findViewById(R.id.info);
        age = (TextView) findViewById(R.id.age);
        location = (TextView) findViewById(R.id.location);
        about = (TextView) findViewById(R.id.about);
        profilePic = (ProfilePictureView) findViewById(R.id.picture);
        profilePic.setPresetSize(ProfilePictureView.NORMAL);

        Log.e("FACEBOOK ID", id);
    }

    @Override
    protected void onResume(){
        super.onResume();
        getUserInformation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        edit_icon = menu.findItem(R.id.edit_icon);

        if(id.equals(getCurrentAccessToken().getUserId())){
            edit_icon.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.edit_icon:
                Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);
                intent.putExtra(ProfileEditActivity.EXTRA_FACEBOOK_ID, id);
                intent.putExtra(ProfileEditActivity.EXTRA_ABOUT_TEXT, about.getText());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getUserInformation(){
        basinWebRequest();
        (new FacebookProfile()).execute(id);
    }

    private void basinWebRequest(){
        basinURL url = new basinURL();
        url.getUserURL(id, "true");
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url.toString(), null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        // Result handling
                        Log.i("Volley Response", response.toString());
                        try{
                            if(!response.getString("about").equals("null")) {
                                about.setText(response.getString("about"));
                            }
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

    private class FacebookProfile extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... id){
            current = new User(id[0]);
            current.startRequest();
            return "success";
        }

        @Override
        protected void onPostExecute(String results){
            ListView listView = (ListView)findViewById(R.id.likes_list);
            listView.setAdapter(new ArrayAdapter<>(ProfileActivity.this,
                    android.R.layout.simple_list_item_1,
                    current.getFacebookLikes()));

            info.setText(current.getName());
            age.setText(current.getBirthday());
            profilePic.setProfileId(current.getFacebookID());
            location.setText(current.getLocation());

            about.setVisibility(View.VISIBLE);
            listContainer.setVisibility(View.VISIBLE);
            loadingPanel.setVisibility(View.GONE);


            Log.e("UI UPDATED:", "SUCCESS");
        }
    }


}
