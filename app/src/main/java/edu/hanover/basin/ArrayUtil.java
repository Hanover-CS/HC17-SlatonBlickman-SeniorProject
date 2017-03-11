package edu.hanover.basin;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;

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

}
