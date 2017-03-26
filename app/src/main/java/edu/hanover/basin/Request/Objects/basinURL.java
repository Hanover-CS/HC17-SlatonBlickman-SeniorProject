package edu.hanover.basin.Request.Objects;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * This object is used to obfuscate the various URLs for basinWeb requests.
 * It allows changes in the URL scheme without having to find the other occurrences of the URL in code.
 * Please note that a basinURL can only model a single URL at a time. Each get...() will overwrite the underlying URL.
 * @author Slaton Blickman
 */
public class basinURL {
    //Instance variables
    private static final String OPEN_BASINWEB = "http://vault.hanover.edu/~blickmans15/services/basinWeb/v1/index.php";
    private String buildURL = "";

    /**
     * Basic constructor that intializes the url to be the home for basinWeb.
     */
    public basinURL(){
        buildURL = OPEN_BASINWEB;
    }

    /**
     * Gets the full URL for basinWeb
     * @return
     */
    public String getBasinWebURL(){
        return OPEN_BASINWEB;
    }
    /**
     * Gets a URL for a particular event.
     * A URL for all events can be done by passing in an empty string.
     * @param id the event id to query in basinWeb
     * @return String representing the URL
     */
    public String getEventURL(String id){
        buildURL = OPEN_BASINWEB + "/events/" + id;
        return buildURL;
    }

    /**
     * Gets a URL for a particular user.
     * A URL for all users can be done by passing in an empty string.
     * Doesn't throw an exception here, but basinWeb will error.
     *
     * TODO: Throw exception for invalid paramater of is_facebook_id
     * TODO: Implement another getUserURL with is_facebook_id as a proper boolean
     *
     * @param id the user id to query in basinWeb
     * @param is_Facebook_id a string that should have value "true" or "false" to use the id as the Facebook_ID
     * @return String representing the URL
     */
    public String getUserURL(String id, String is_Facebook_id){
        if(!(is_Facebook_id.equals("true") || is_Facebook_id.equals("false"))){
            Log.e("Invalid parameter", is_Facebook_id);
            throw new IllegalArgumentException("is_facebook_id must be 'true' or 'false'");
        }
        buildURL = OPEN_BASINWEB + "/users/" + id + "?facebook_id=" + is_Facebook_id;
        return buildURL;
    }

    /**
     * Gets a URL for a particular user.
     * A URL for all users can be done by passing in an empty string.
     * @param id the event id to query in basinWeb
     * @return String representing the URL
     */
    public String getUserURL(String id){
        buildURL = OPEN_BASINWEB + "/users/" + id;
        return buildURL;
    }

    /**
     * Gets a URL for the events related to a user.
     * @param user_id the id for the corresponding user
     * @param params the parameters to attach to the URL for a more specific request; ie ["facebook_id" : true]
     * @return String representing the URL
     */
    public String getUserEventsURL(String user_id, HashMap<String, String> params){
        if(user_id.equals("")){
            throw new IllegalArgumentException("user_id cannot be empty string");
        }

        buildURL = getUserURL(user_id) + "/events";
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

    /**
     * Gets a URL for the attendees of an event
     * @param event_id the id for the corresponding event
     * @return String representing the URL
     */
    public String getEventAttendeesURL(String event_id){
        if(event_id.equals("")){
            throw new IllegalArgumentException("event_id cannot be an empty string");
        }
        else{
            buildURL = getEventURL(event_id) + "/attendees";
        }
        return buildURL;
    }

    /**
     * Gets a URL to determine whether a particular user is attending a particular event
     * @param event_id the id corresponding to the event
     * @param user_id the id corresponding to the user you want check is attending the event
     * @return String representing the URL
     */
    public String getIsAttendingURL(String event_id, String user_id){
        if(event_id.equals("") || user_id.equals("")){
            throw new IllegalArgumentException("ids cannot be empty");
        }
        buildURL = getEventAttendeesURL(event_id) + "/" + user_id;
        return buildURL;
    }

    /**
     * Gets the basinURL that has been built by the last method call in a string representation.
     * This can be used for Volley requests.
     * @return String
     */
    public String toString(){
        return buildURL;
    }
}
