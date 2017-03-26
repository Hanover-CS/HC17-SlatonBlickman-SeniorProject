package edu.hanover.basin.Users.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import edu.hanover.basin.R;
import edu.hanover.basin.Request.Objects.basinURL;
import edu.hanover.basin.Users.Objects.User;

/**
 * Activity for handling a single editText view that will be used to edit a User's about section for their Profile.
 *
 * TODO: Submit for Facebook review to be able to display likes, birthday, and location
 *
 * @author Slaton Blickman
 * @see AppCompatActivity
 * @see User
 */
public class ProfileEditActivity extends AppCompatActivity {
    //Intent Extras
    /**
     * Field for getting and putting the Facebook ID of the user
     */
    public static final String EXTRA_FACEBOOK_ID = "UserFacebookID";

    /**
     *  Field for getting and putting the default text for the editText
     */
    public static final String EXTRA_ABOUT_TEXT = "AboutText";

    //View variables
    private TextView edit_about;

    //Strings to remember
    private String id;
    private String about;


    /**
     * Gets the id and about section from the intent.
     * Initializes about text to be the data from the intent.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        id = getIntent().getStringExtra(EXTRA_FACEBOOK_ID);
        about = getIntent().getStringExtra(EXTRA_ABOUT_TEXT);

        edit_about = (TextView)findViewById(R.id.about);
        edit_about.setText(about);
    }

    /**
     * Inflate the menu laying using menu_edit.xml
     * @param menu
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);

        return true;
    }

    /**
     * Handles the clicks of hte menu items.
     * Does the following:
     * (save_icon) Executes a request to basinWeb to update the user's information
     * (cancel_icon) finishes the current Activity so that it closes.
     * @param item menuItem that was clicked
     * @return boolean for success
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.save_icon:
                if(edit_about.getText().length() > 0){
                    basinRequest();
                }
                return true;
            case R.id.cancel_icon:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //does a Volley JsonObjectRequest to update the about section
    private void basinRequest(){
        basinURL url = new basinURL();
        JSONObject body = new JSONObject();

        //specify that we are using the Facebook_id for basinWeb
        url.getUserURL(id, "true");

        try{
            body.put("about", edit_about.getText());
        }
        catch(JSONException e){
            Log.e("JSONEXCEPTION", e.toString());
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.PUT, url.toString(), body,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // Result handling
                        Log.i("Volley Response", response.toString());
                        Toast.makeText(ProfileEditActivity.this, "Updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();

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


}
