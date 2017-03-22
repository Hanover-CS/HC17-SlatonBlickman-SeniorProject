package edu.hanover.basin.Request.Activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import edu.hanover.basin.R;
import edu.hanover.basin.Request.Objects.basinWebRequest;
import edu.hanover.basin.Request.Objects.basinURL;

@SuppressWarnings("ALL")
public class BasinWebTestActivity extends Activity {
    public static final String EXTRA_FACEBOOK_ID = "UserFacebookID";
    String fb_id;
    TextView test;
    basinWebRequest request;
    basinWebRequest volleyTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basin_web_test);
        test = (TextView)findViewById(R.id.results);
        fb_id = (String)getIntent().getExtras().get(EXTRA_FACEBOOK_ID);
        volleyTest = new basinWebRequest(this);
    }

    public void onClickUsers(View v){
        test.setText("executing task");
        (new GetAllUsers()).execute("users");

    }

    public void onClickMyEvents(View v){
        test.setText("executing task");
        (new GetAllUsers()).execute("events");
    }

    public void onClickVolley(View v){
//        test.setText("executing task");
//        volleyTest.VolleyData("users/" + fb_id + "/events?facebook_id=true");
//        Log.i("BUTTON RESPONSE", volleyTest.getJSON().toString());
//        while(volleyTest.getJSON().toString().equals("{empty: empty}")){
//            volleyTest.VolleyData("users/" + fb_id + "/events?facebook_id=true");
//            test.setText(volleyTest.getJSON().toString());
//        }

        basinURL burl = new basinURL();
        HashMap<String, String> params = new HashMap<>();
        params.put("facebook_id", "true");
        burl.getUserEventsURL(fb_id, params);
        Log.i("BASIN URL", burl.toString());
        request(burl.toString());


    }

    private void request(String url){
        // Request a string response
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        // Result handling
                        try {
                            test.setText(response.toString(3));
                            Log.i("Volley Response", response.toString());
                        }
                        catch(JSONException e){
                            Log.e("JSON EXCEPTION", e.toString());
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                // Error handling
                test.setText("Error Processing Request");
                Log.e("Volley error", "Something went wrong!");
                error.printStackTrace();

            }

        });

        // Add the request to the queue
        Volley.newRequestQueue(this).add(stringRequest);

    }
    private class GetAllUsers extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params){
            String r;
            Log.i("DOING IN BG:", params[0]);
            request = new basinWebRequest();
            Log.i("params", params[0]);
            switch(params[0]){
                case "users":
                    r = request.GET("users");
                    Log.i("EXECUTING FOR USERS: ", r);
                    return r;
                case "events":
                    r = request.GET("users/" + fb_id + "/events?facebook_id=true");
                    return r;
                default:
                    return "no case";
            }

        }

        @Override
        protected void onPostExecute(String results){
            Log.i("RESULTS: ", results);
            test.setText(results);
//            try {
//                JSONArray users = request.getJSON().getJSONArray("users");
//                test.setText(users.toString());
//            }
//            catch(JSONException e){
//                Log.i("JSON ERROR", e.toString());
//            }
        }


        }


}
