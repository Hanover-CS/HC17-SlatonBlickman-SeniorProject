package edu.hanover.basin;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

public class ProfileActivity extends Activity {
    public static final String EXTRA_FACEBOOK_ID = "UserFacebookID";

    private ProfilePictureView profilePic;
    private TextView info;
    private TextView age;
    private TextView location;

    private User current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        String id = (String)getIntent().getExtras().get(EXTRA_FACEBOOK_ID);
        info = (TextView) findViewById(R.id.info);
        age = (TextView) findViewById(R.id.age);
        location = (TextView) findViewById(R.id.location);
        profilePic = (ProfilePictureView) findViewById(R.id.picture);
        Log.e("FACEBOOK ID", id);
        (new UpdateProfile()).execute(id);
    }

    private class UpdateProfile extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... id){
            current = new User(id[0]);
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

            Log.e("UI UPDATED:", "SUCCESS");

        }
    }
}
