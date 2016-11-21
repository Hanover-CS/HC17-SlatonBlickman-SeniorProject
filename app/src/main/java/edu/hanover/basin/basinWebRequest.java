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

    public JSONObject results;

    public String getUsers(){
        try {
            URL url = new URL(OPEN_BASINWEB + "users");
            try {
                Log.i("EXECUTING GET USERS", url.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                int status = conn.getResponseCode();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line+"\n");
                }
                br.close();
                try{
                    this.results = new JSONObject(sb.toString());
                }
                catch(JSONException e){
                    return e.toString();
                }
                return sb.toString();


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
