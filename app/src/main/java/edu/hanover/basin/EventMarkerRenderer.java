package edu.hanover.basin;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by Slaton on 3/12/2017.
 */

public class EventMarkerRenderer extends DefaultClusterRenderer<EventMarker> {

    public EventMarkerRenderer(Context context, GoogleMap map, ClusterManager<EventMarker> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onClusterItemRendered(EventMarker eventMarker,
                                         Marker marker) {
        super.onClusterItemRendered(eventMarker, marker);

        //other stuff......
    }
}
