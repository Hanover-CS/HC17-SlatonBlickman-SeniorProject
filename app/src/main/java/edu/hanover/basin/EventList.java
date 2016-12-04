package edu.hanover.basin;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Slaton on 12/4/2016.
 */

public class EventList {
    private ArrayList eventsArrayList;

    ArrayList EventList(JSONArray events){
        JSONObject objIn;
        try {
            for (int i = 0; i < events.length(); i++) {
                objIn = events.getJSONObject(i);
                eventsArrayList.add(objIn);

            }
        }
        catch(JSONException e){
            Log.e("eventList constructor", e.toString());
        }
        return eventsArrayList;
    }
}
