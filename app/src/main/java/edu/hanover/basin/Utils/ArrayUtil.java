package edu.hanover.basin.Utils;

import android.util.Log;

import com.google.maps.android.clustering.Cluster;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

import edu.hanover.basin.Map.Objects.EventMarker;

/**
 * A class to hold Array utilities
 *
 * Created by Slaton on 3/11/2016.
 */

public class ArrayUtil {

    /**
     * Copies JSONArray of JSONObjects to an ArrayList with same content
     * Primarily useful for turning Volley request results into an ArrayAdapter usable format for ListViews
     * @param jsonArray the original JSONArray to be copied
     * @return ArrayList
     */
    public static ArrayList<JSONObject> toArrayList(JSONArray jsonArray){
        ArrayList<JSONObject> arrayList;
        JSONObject objIn;

        arrayList = new ArrayList<>();

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

    /**
     * Copies Cluster of EventMarkers to an ArrayList with same content
     * Primarily useful for turning Clusters rendered on Google Maps into an ArrayAdapter usable format for ListViews
     * @param cluster the original cluster to be copied
     * @return ArrayList
     * @see edu.hanover.basin.Map.Objects.EventClusterRenderer
     */
    public static ArrayList<EventMarker> toArrayList(Cluster<EventMarker> cluster){
        ArrayList<EventMarker> arrayList;
        JSONObject objIn;

        arrayList = new ArrayList<>();
        Collection<EventMarker> collection = cluster.getItems();
        //arrayList = new ArrayList<EventMarker>(Arrays.asList(collection.toArray()));

        for(EventMarker marker : collection){
            arrayList.add(marker);
        }

        return arrayList;
    }

}
