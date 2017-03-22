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

public class ProfileEditActivity extends AppCompatActivity {
    public static final String EXTRA_FACEBOOK_ID = "UserFacebookID";
    public static final String EXTRA_ABOUT_TEXT = "AboutText";

    private TextView edit_about;

    private String id;
    private String about;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        id = getIntent().getStringExtra(EXTRA_FACEBOOK_ID);
        about = getIntent().getStringExtra(EXTRA_ABOUT_TEXT);

        edit_about = (TextView)findViewById(R.id.about);
        edit_about.setText(about);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        Intent intent;
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

    private void basinRequest(){
        basinURL url = new basinURL();
        JSONObject body = new JSONObject();

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
