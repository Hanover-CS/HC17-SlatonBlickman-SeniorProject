package edu.hanover.basin;

import android.hardware.camera2.params.Face;
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
 * Created by Slaton on 11/5/2016.
 */

public class User {
    private String FacebookID;
    private int id;
    private String name;
    private String birthday;
    private String link;
    private String location;

    private List<String> FacebookLikes = new ArrayList<>();

    User(String id){
        this.FacebookID = id;
        requestUserInfo();

    }

    User(final AccessToken accessToken){
        Log.e("CREATE USER", accessToken.toString());
        requestCurrentUserInfo(accessToken);
    }

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


    private void requestLikes(){
        //make callback function
        final GraphRequest.Callback graphCallback = new GraphRequest.Callback(){
            @Override
            public void onCompleted(GraphResponse response) {
                try{
                    if (response.getJSONObject() != null) {
                        JSONArray data = response.getJSONObject().getJSONArray("data");
                        //Log.e("DATA LIKES: ", data.toString());
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject objectIn = data.getJSONObject(i);
                            String like = objectIn.getString("name");
                            FacebookLikes.add(like);
                        }
                        GraphRequest nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                        if (nextRequest != null) {
                            nextRequest.setCallback(this);
                            nextRequest.executeAndWait();
                        }
                    }
                    else{
                        Log.e("RESPONSE ERROR: ", response.toString());
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
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
        Log.e("this request:",  "/"+ FacebookID +"/likes");


    }

    private void requestUserInfo(){
        Bundle param = new Bundle();
        param.putString("fields", "id,name,link,birthday");

        GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/"+ FacebookID,
                param,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try{
                            JSONObject json = response.getJSONObject();
                            name = json.getString("name");
                            birthday = json.getString("birthday");
                            link = json.getString("link");
                            requestLikes();

                        }
                        catch(JSONException e){
                            Log.e("JSON EXCEPTION!", e.toString());
                        }

                    }
                }
        );
    }

    public List<String> commonLikes(User user2){
        return FacebookLikes;
    }

    public void getTags(){

    }

    public String getFacebookID(){
        return FacebookID;
    }

    public List<String> getFacebookLikes(){
        return FacebookLikes;
    }

    public String getName(){
        return name;
    }

    public String getBirthday(){
        return birthday;
    }

    public String getLink(){
        return link;
    }

    public int getDatabaseID(){
        return id;
    }

    public String getLocation(){
        return location;
    }
}


