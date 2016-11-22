package edu.hanover.basin;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.AccessToken;

import org.json.JSONArray;
import org.json.JSONException;

public class BasinWebTestActivity extends Activity {
    public static final String EXTRA_FACEBOOK_ID = "UserFacebookID";
    String fb_id;
    TextView test;
    basinWebRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basin_web_test);
        test = (TextView)findViewById(R.id.results);
        fb_id = (String)getIntent().getExtras().get(EXTRA_FACEBOOK_ID);
    }

    public void onClickUsers(View v){
        test.setText("executing task");
        (new GetAllUsers()).execute("users");

    }

    public void onClickMyEvents(View v){
        test.setText("executing task");
        (new GetAllUsers()).execute("events");
    }

    private class GetAllUsers extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params){
            Log.i("DOING IN BG:", params[0]);
            request = new basinWebRequest();
            Log.i("params", params[0]);
            switch(params[0]){
                case "users":
                    String k = request.GET("users");
                    Log.i("EXECUTING FOR USERS: ", k);
                    return k;
                case "events":
                    String r = request.GET("users/" + fb_id + "/events?facebook_id=true");
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
