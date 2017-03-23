package edu.hanover.basin.Events.Objects;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Constructs an EventList object.
 * The purpose of this was to provide an object for JSONArray conversion, but ArrayUtils methods should be used instead.
 *
 * @author Slaton Blickman
 * @deprecated
 */
@Deprecated
public class EventList {
    private final ArrayList<JSONObject> eventsArrayList;

    /**
     * Constructs EventList to hold an ArrayList of JSONObjects that represent events
     * @param events the JSONarray to be converted
     */
    public EventList(JSONArray events){
        JSONObject objIn;
        eventsArrayList = new ArrayList<>();

        try {
            for (int i = 0; i < events.length(); i++) {
                objIn = events.getJSONObject(i);

                eventsArrayList.add(objIn);

            }
        }
        catch(JSONException e){
            Log.e("eventList constructor", e.toString());
        }
    }

    /**
     * Gets the list of Events as an ArrayList
     * @return ArrayList
     */
    ArrayList<JSONObject> toArrayList(){
        return eventsArrayList;
    }
}
