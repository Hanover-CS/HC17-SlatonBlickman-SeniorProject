package edu.hanover.basin;

import android.util.Log;

import com.google.maps.android.clustering.Cluster;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Slaton on 3/11/2016.
 */

public class ArrayUtil {

    public static ArrayList<JSONObject> toArrayList(JSONArray jsonArray){
        ArrayList<JSONObject> arrayList;
        JSONObject objIn;
        arrayList = new ArrayList<JSONObject>();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                objIn = jsonArray.getJSONObject(i);

                arrayList.add(objIn);

            }
        }
        catch(JSONException e){
            Log.e("eventList constructor", e.toString());
        }
        return arrayList;
    }

    public static ArrayList<EventMarker> toArrayList(Cluster<EventMarker> cluster){
        ArrayList<EventMarker> arrayList;
        JSONObject objIn;
        arrayList = new ArrayList<EventMarker>();
        Collection<EventMarker> collection = cluster.getItems();
        //arrayList = new ArrayList<EventMarker>(Arrays.asList(collection.toArray()));

        for(EventMarker marker : collection){
            arrayList.add(marker);

        }

        return arrayList;
    }

}
