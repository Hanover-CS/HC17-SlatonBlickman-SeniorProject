package edu.hanover.basin.Users.Objects;

import android.os.Bundle;
import android.util.Log;


import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This object represents a Facebook User and the information basin has permission to access.
 * Methods MUST only be used inside AsyncTask as it executes GraphRequests.
 * Please note that some information is not accessible until the Facebook Review completes.
 *
 * @author Slaton BLickman
 * @see android.os.AsyncTask
 * @see GraphRequest
 * @see GraphResponse
 */

@SuppressWarnings("ALL")
public class User {

    //Instance variables
    private String FacebookID;
    private int id;
    private String name;
    private String birthday;
    private String link;
    private String location;
    private String url;
    private final List<String> FacebookLikes;

    //determines whether or not a likes request should be made
    private boolean shouldGetLikes;

    /**
     * Constructs person object to prepare it for requests.
     * shouldGetLikes always defaults to false; it needs to be set to true seperately since it can be time-consuming.
     * @param id the facebook ID for the user to request data
     */
    public User(String id){
        FacebookLikes = new ArrayList<>();

        shouldGetLikes = true;
        this.FacebookID = id;
        url = "/" + FacebookID;

        Log.i("NEW USER(id):", id);

    }

    /**
     * Constructs person object to prepare it for requests.
     * This method should has a redundant paramater as accessToken since the accessToken
     * is always accessible. Use of this constructor means a newMeRequest will be made instead of a
     * generic GraphRequest
     * shouldGetLikes defaults to true here.
     *
     * Use of this is not recommended.
     *
     * @deprecated
     * @param id the facebook ID for the user to request data
     */
    @Deprecated
    public User(final AccessToken accessToken){
        FacebookLikes = new ArrayList<>();
        Log.e("NEW USER(token)", accessToken.toString());
        shouldGetLikes = true;
        requestCurrentUserInfo(accessToken);
    }

    /**
     * Call this to begin requesting user information inside your AsyncTask
     */
    public void startRequest(){
        requestUserInfo();
    }

    /**
     * Determines whether or not to also request Facebook likes for the user.
     * This function has no effect if used after the call to startRequest()
     * @param should true to request likes; false otherwise
     * @return boolean
     */
    public boolean doLikes(boolean should){
        shouldGetLikes = should;
        return shouldGetLikes;
    }

    //this method is not used anywhere and does not need to be
    private void requestCurrentUserInfo(final AccessToken accessToken){
        GraphRequest request = GraphRequest.newMeRequest(accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object,GraphResponse response) {

                        JSONObject json = response.getJSONObject();
                        try {
                            if(json != null){
                                name = json.getString("name");
                                birthday = json.getString("birthday");
                                FacebookID = json.getString("id");
                                link = json.getString("link");
                                JSONObject objectIn = json.getJSONObject("location");
                                location = objectIn.getString("name");
                                requestLikes();

                            }
                            else{
                                Log.e("RESPONSE ERROR: ", response.toString());
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("JSON EXCEPTION!", e.toString());
                        }
                    }
                }
        );
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,birthday,location");
        request.setParameters(parameters);
        request.executeAndWait();
        Log.e("REQUEST COMPLETE:", name + "");

    }

    //use GraphRequest to get user information
    private void requestUserInfo(){
        Log.i("REQUEST INFO", "REQUESTING FOR " + FacebookID);

        Bundle param = new Bundle();

        param.putString("fields", "id,link,name,birthday,location");

        GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                url,
                param,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        try{
                            Log.i("FACEBOOK RESPONSE", response.toString());
                            JSONObject json = response.getJSONObject();
                            Log.i("RETURN JSON", json.toString());

                            if(shouldGetLikes){
                                requestLikes();
                            }

                            name = json.getString("name");
                            birthday = json.getString("birthday");
                            link = json.getString("link");
                            JSONObject objectIn = json.getJSONObject("location");
                            location = objectIn.getString("name");
                        }
                        catch(JSONException e){
                            Log.e("FACEBOOK EXCEPTION!", e.toString());
                        }
                    }
                }
        );

        request.executeAndWait();
    }

    //uses GraphRequest to get user likes
    //uses CallBack methods to handle likes paging
    private void requestLikes(){
        //make callback function
        final GraphRequest.Callback graphCallback = new GraphRequest.Callback(){
            @Override
            public void onCompleted(GraphResponse response) {
                Log.i("Likes received", response.toString());
                try{
                    if (response.getJSONObject() != null) {
                        //date is the list of likes
                        JSONArray data = response.getJSONObject().getJSONArray("data");

                        //loop through the array to add the likes to the local list
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject objectIn = data.getJSONObject(i);
                            String like = objectIn.getString("name");
                            FacebookLikes.add(like);
                        }

                        //Check if there is a next page of likes
                        GraphRequest nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                        if (nextRequest != null) {
                            //setCallback to this function if there is another page and execute the request
                            nextRequest.setCallback(this);
                            nextRequest.executeAndWait();
                        }
                    }
                    else{
                        Log.e("RESPONSE ERROR: ", response.toString());
                    }
                }
                catch (JSONException e) {
                    Log.e("RESPONSE ERROR: ", response.toString());
                }
            }
        };

        /* make the API call */
        Bundle param = new Bundle();
        param.putString("fields", "id,name,category");
        //send first request, the rest should be called by the callback
        GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/"+ FacebookID +"/likes",param, HttpMethod.GET, graphCallback);

        request.executeAndWait();

    }

    /**
     * A function to get a list common likes between two users
     *
     * TODO: All implementation. This a stub.
     *
     * @param user2 a user to compare this user to
     * @return List representing common likes
     */
    public List<String> commonLikes(User user2){
        return FacebookLikes;
    }

    /**
     * Gets the facebook ID of the user
     * @return String
     */
    public String getFacebookID(){
        return FacebookID;
    }

    /**
     * Gets the FacebookLikes for the user
     * @return List
     */
    public List<String> getFacebookLikes(){
        return FacebookLikes;
    }

    /**
     * Gets the name of the user
     * @return String
     */
    public String getName(){
        return name;
    }

    /**
     * Gets the Birthday of the user
     * @return String
     */
    public String getBirthday(){
        return birthday;
    }

    /**
     * Gets the link of the user
     * @return String
     */
    public String getLink(){
        return link;
    }

    /**
     * Gets location of the user
     * @return String
     */
    public String getLocation(){
        return location;
    }
}


