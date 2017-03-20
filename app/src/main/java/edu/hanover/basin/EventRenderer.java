package edu.hanover.basin;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Slaton on 3/19/2017.
 */

public class EventRenderer extends DefaultClusterRenderer<EventMarker> {
    public EventRenderer(Context context, GoogleMap map,
                         ClusterManager<EventMarker> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onClusterItemRendered(EventMarker clusterItem, Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);

    }

    @Override
    protected void onBeforeClusterRendered(Cluster<EventMarker> cluster, MarkerOptions markerOptions) {
        super.onBeforeClusterRendered(cluster, markerOptions);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<EventMarker> cluster){
        if(cluster.getSize() > 1){
            return true;
        }
        return false;
    }
}
