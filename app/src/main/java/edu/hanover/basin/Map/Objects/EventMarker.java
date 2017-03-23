package edu.hanover.basin.Map.Objects;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * An object that implements ClusterItems as basin Events to hold extra information
 * @autor Slaton Blickman
 * @see ClusterItem
 */

public class EventMarker implements ClusterItem {
    private final LatLng mPosition;
    private final String mTitle;
    private final String mSnippet;
    private final String mId;

    /**
     * Constructor for EventMarker rendered through Cluster
     * Note: id defaults to -1 and items will not have titles nor snippets
     * @param lat latitude for the location
     * @param lng longitude for the location
     */
    public EventMarker(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
        mTitle = "";
        mSnippet = "";
        mId = "-1";
    }

    /**
     * Advanced Constructor for EventMarkers rendered through Cluster
     * Should always be used
     * @param lat latitude for the location
     * @param lng longitude for the location
     * @param title the title for the marker to be rendered
     * @param snippet the snippet for the marker be rendered
     * @param id the event id presumably from basinWeb
     */
    public EventMarker(double lat, double lng, String title, String snippet, String id) {
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mSnippet = snippet;
        mId = id;
    }

    /**
     * Function for getting the event id
     * @return String for the event id
     */
    public String getID(){
        return mId;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }
}
