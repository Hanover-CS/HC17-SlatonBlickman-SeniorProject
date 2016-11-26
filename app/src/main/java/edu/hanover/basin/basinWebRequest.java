package edu.hanover.basin;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Slaton on 11/21/2016.
 */

public class basinWebRequest {
    private static final String OPEN_BASINWEB = "http://10.0.2.2/basinWeb/v1/index.php/";
    //FIX THE OPEN_BASINWEB to go to the correct IP when not in emulator

    private Context context;
    private JSONObject results;

    public JSONObject getJSON(){
        return results;
    }

    private void toJSON(String object){
        try{
            this.results = new JSONObject(object);
        }
        catch(JSONException e){
            Log.e("JSON ERROR", e.toString());
        }
    }


    basinWebRequest(Context context){
        this.context = context;
        toJSON("{empty: empty}");
    }

    basinWebRequest(){
        toJSON("{empty: empty}");
    }

    public String GET(String route){
        return requestData(route, "GET");
    }

    public JSONObject VolleyData(String route){
        String url = OPEN_BASINWEB + route;

        // Request a string response
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        // Result handling
                        toJSON(response);
                        Log.i("Volley Response", getJSON().toString());

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                // Error handling
                Log.e("Volley error", "Something went wrong!");
                error.printStackTrace();

            }
        });

        // Add the request to the queue
        Volley.newRequestQueue(context).add(stringRequest);
        return results;
    }

    //take body and parameters as well
    private String requestData(String route, String method){
        try {
            URL url = new URL(OPEN_BASINWEB + route);
            try {
                Log.i("EXECUTING GET " + route, url.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);

                //may
                conn.connect();
                //do stuff with
                int status = conn.getResponseCode();

                //do stuff with different statuses

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line+"\n");
                }
                br.close();
                try{
                    this.results = new JSONObject(sb.toString());
                    return this.results.toString(3);
                }
                catch(JSONException e){
                    return e.toString();
                }


            }
            catch(IOException e){
                return e.toString();
            }
        }
        catch(MalformedURLException e){
            return e.toString();
        }


    }

}
