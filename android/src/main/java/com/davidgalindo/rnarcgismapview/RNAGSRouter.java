package com.davidgalindo.rnarcgismapview;

import android.content.Context;
import android.util.Log;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.tasks.networkanalysis.Route;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask;
import com.esri.arcgisruntime.tasks.networkanalysis.Stop;


import java.util.ArrayList;
import java.util.List;

public class RNAGSRouter {
    // MARK: Properties
    private RouteTask routeTask;
    private RouteParameters routeParameters;

    public RNAGSRouter(Context context, String routeUrl) {
        routeTask = new RouteTask(context, routeUrl);
        routeTask.loadAsync();
        routeTask.addDoneLoadingListener(() -> {
            ListenableFuture<RouteParameters> future = routeTask.createDefaultParametersAsync();
            future.addDoneListener(() -> {
                try {
                    routeParameters = future.get();
                    routeParameters.setOutputSpatialReference(SpatialReferences.getWgs84());
                } catch (Exception e) {
                    Log.w("WARNING (AGS)", "Routing parameters failed to load. Perhaps the app has not received valid credentials?");
                    e.printStackTrace();
                }
            });
        });
    }

    public ListenableFuture<RouteResult> createRoute(GraphicsOverlay graphicsOverlay,ArrayList<String> excludeGraphics) {
        // Clear stops
        if (routeParameters == null) {
            Log.w("WARNING (AGS)", "It looks like the Esri Routing service is down, or you did not provide valid credentials. Please try again later, or submit an issue.");
            return null;
        }
        routeParameters.clearStops();
        // I know this is deprecated but it just works ._. setStops does not work... good job, Esri
        // See https://developers.arcgis.com/android/latest/api-reference/reference/com/esri/arcgisruntime/tasks/networkanalysis/RouteParameters.html
        List<Stop> stops = routeParameters.getStops();
        for (Graphic item : graphicsOverlay.getGraphics()) {
            String referenceId = ((String) item.getAttributes().get("referenceId"));
            if (excludeGraphics == null || !(excludeGraphics.contains(referenceId))) {
                // Reform the point with spatial reference
                Point point = ((Point) item.getGeometry());
                stops.add(new Stop(point));
            }
        }
        routeParameters.setOutputSpatialReference(SpatialReferences.getWgs84());
        return routeTask.solveRouteAsync(routeParameters);
    }
}
