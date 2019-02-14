package com.davidgalindo.rnarcgismapview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
    // TODO: Modify so user can generate a token insetad of using your own
    private String routeUrl = "https://route.arcgis.com/arcgis/rest/services/World/Route/NAServer/Route_World?token=QQg9AddP1EeyirFVqnfs_EzBDXJxX352qz1VMN1HVytTku5rOQBJP2106LCGptqSJ-vv7rnMApwqf358J8QB1gQTS7Z-RNUtHCcKoXxRkxdjGVJy8RVLSowxjiWviw0yx1Th1BumF5Un6FI2yhaQBVxKkbXSA0bOE1DGX_A7DCRynWGCsGK-L8z9ZJiD_wFuywKCX_qx5iIb3owsrc7Aqw..";
    private RouteParameters routeParameters;

    public RNAGSRouter(Context context) {
        routeTask = new RouteTask(context, routeUrl);
        routeTask.loadAsync();
        routeParameters = new RouteParameters();
        routeTask.addDoneLoadingListener(() -> {
            ListenableFuture<RouteParameters> future = routeTask.createDefaultParametersAsync();
            future.addDoneListener(() -> {
                try {
                    RouteParameters result = future.get();
                    routeParameters.setTravelMode(result.getTravelMode());
                    routeParameters.setOutputSpatialReference(SpatialReferences.getWgs84());
                } catch (Exception e) {
                    Log.w("WARNING (AGS)", "Routing parameters failed to load. Perhaps the app has not received valid credentials?");
                    e.printStackTrace();
                }
            });
        });
    }

    public Route createRoute(@NonNull GraphicsOverlay graphicsOverlay, @Nullable ArrayList<String> excludeGraphics){
        // Clear stops
        if (routeParameters == null) {
            Log.w("WARNING (AGS)", "It looks like the Esri Routing service is down, or you did not provide valid credentials. Please try again later, or submit an issue.");
            return null;
        }
        routeParameters.clearStops();
        // I know this is deprecated but it just works ._. setStops does not work... good job, Esri
        // See https://developers.arcgis.com/android/latest/api-reference/reference/com/esri/arcgisruntime/tasks/networkanalysis/RouteParameters.html
        List<Stop> stops = routeParameters.getStops();
        for (Graphic item: graphicsOverlay.getGraphics()) {
            String referenceId = ((String) item.getAttributes().get("referenceId"));
            if (excludeGraphics == null || !(excludeGraphics.contains(referenceId))) {
                // Reform the point with spatial reference
                Point point = ((Point) item.getGeometry());
                stops.add(new Stop(point));
            }
        }
        routeParameters.setOutputSpatialReference(SpatialReferences.getWgs84());
        ListenableFuture<RouteResult> future  = routeTask.solveRouteAsync(routeParameters);
        try {
            RouteResult result = future.get();
            if (result == null || result.getRoutes().isEmpty()) {
                return null;
            }
            return result.getRoutes().get(0);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
