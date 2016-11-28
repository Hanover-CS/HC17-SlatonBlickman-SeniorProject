package edu.hanover.basin;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Slaton on 11/27/2016.
 */

public class basinURL {
    private static final String OPEN_BASINWEB = "http://10.0.2.2/basinWeb/v1/index.php/";
    private String buildURL = "";

    public String getUserURL(String id, String is_Facebook_id){
        buildURL = OPEN_BASINWEB + "/users/" + id + "?facebook_id=" + is_Facebook_id;
        return buildURL;
    }

    public String getUserEventsURL(String id, String is_Facebook_id, HashMap<String, String> params){
        getUserURL(id, is_Facebook_id);
        buildURL += "/events";
        if(!params.isEmpty() || params != null) {
            buildURL += "?";
            int i = 1;
            for (Map.Entry param : params.entrySet()) {
                buildURL = param.getKey() + "=" + param.getValue();
                if(i < params.size()){
                    buildURL += ",";
                }
                i++;
            }
        }

        return buildURL;
    }

    public String getUserEventsURL(String id, String is_Facebook_id){
        return getUserEventsURL(id, is_Facebook_id, null);
    }
}
