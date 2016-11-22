package edu.hanover.basin;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
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

    private JSONObject results;

    public JSONObject getJSON(){
        return results;
    }


    public String GET(String route){
        try {
            URL url = new URL(OPEN_BASINWEB + route);
            try {
                Log.i("EXECUTING GET " + route, url.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

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
