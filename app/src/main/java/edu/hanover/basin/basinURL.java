package edu.hanover.basin;

import android.content.ServiceConnection;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Slaton on 11/27/2016.
 */

public class basinURL {
    private static final String OPEN_BASINWEB = "http://10.0.2.2/basinWeb/v1/index.php";
    private String buildURL = "";

    basinURL(){
        buildURL = OPEN_BASINWEB;
    }

    public String getEventURL(String id){
        buildURL = OPEN_BASINWEB + "/events/" + id;
        return buildURL;
    }

    public String getUserURL(String id, String is_Facebook_id){
        buildURL = OPEN_BASINWEB + "/users/" + id + "?facebook_id=" + is_Facebook_id;
        return buildURL;
    }

    public String getUserURL(String id){
        buildURL = OPEN_BASINWEB + "/users/" + id;
        return buildURL;
    }
    public String getUserEventsURL(String id, HashMap<String, String> params){
        buildURL = getUserURL(id) + "/events";
        if(!params.isEmpty()) {
            buildURL += "?";
            int i = 1;
            for (Map.Entry param : params.entrySet()) {
                buildURL += param.getKey() + "=" + param.getValue();
                if(i < params.size()){
                    buildURL += "&";
                }
                i++;
            }
        }
        return buildURL;
    }

    public String postEventURL(){
        buildURL = OPEN_BASINWEB + "/events";
        return buildURL;
    }


    public String toString(){
        return buildURL;
    }
}
