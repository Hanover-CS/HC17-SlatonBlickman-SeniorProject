package edu.hanover.basin.Map.Objects;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import edu.hanover.basin.Map.Objects.EventMarker;

/**
 * A custom renderer for Events on the Google Maps fragment through the ClusterManager.
 * Overrides default behavior of Cluster and ClusterItem rendering.
 * Changes Clusters to render for any plural size.
 *
 * @autor Slaton Blickman
 * @see DefaultClusterRenderer
 * @see ClusterManager
 */

public class EventClusterRenderer extends DefaultClusterRenderer<EventMarker> {
    public EventClusterRenderer(Context context, GoogleMap map,
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
        return (cluster.getSize() > 1);
    }
}
