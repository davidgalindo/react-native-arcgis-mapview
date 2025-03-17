package com.davidgalindo.rnarcgismapview;

import androidx.annotation.Nullable;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

public class RNArcGISMapViewManager extends SimpleViewManager<RNAGSMapView> {
    // MARK: Constants
    private final int SHOW_CALLOUT = 1;
    private final int CENTER_MAP = 2;
    private final int ADD_GRAPHICS_OVERLAY = 3;
    private final int REMOVE_GRAPHICS_OVERLAY = 4;
    private final int ADD_POINTS_TO_OVERLAY = 5;
    private final int REMOVE_POINTS_FROM_OVERLAY = 6;
    private final int UPDATE_POINTS_IN_GRAPHICS_OVERLAY = 7;
    private final int ROUTE_GRAPHICS_OVERLAY = 8;
    private final int SET_ROUTE_IS_VISIBLE = 9;
    private final int RE_LOAD_MAP = 10;

  private final int DISPOSE = 999;

    // MARK Initializing methods
    @Override
    public String getName() {
        return "RNArcGISMapView";
    }

    @Override
    protected RNAGSMapView createViewInstance(ThemedReactContext reactContext) {
        return new RNAGSMapView(reactContext);
    }

    // MARK: Props
    @ReactProp(name = "basemapUrl")
    public void setBasemapUrl(RNAGSMapView view, @Nullable String url) {
        if (url != null && !url.isEmpty()) {
            view.setBasemapUrl(url);
        }
    }

    @ReactProp(name = "routeUrl")
    public void setRouteUrl(RNAGSMapView view, @Nullable String url) {
        if (url != null && !url.isEmpty()) {
            view.setRouteUrl(url);
        }
    }

    @ReactProp(name = "initialMapCenter")
    public void setInitialMapCenter(RNAGSMapView view, @Nullable ReadableMap array) {
      ReadableArray listPoint=array.getArray("points");
      Integer stroke=1;
      Double scale=0.5;
      if(
       array.hasKey("stroke")
      ){
        stroke=array.getInt("stroke");
     }
     if( array.hasKey("mapScale")) {
       scale = array.getDouble("mapScale");
     }
      view.setInitialMapCenter(listPoint,stroke,scale);
    }

    @ReactProp(name = "recenterIfGraphicTapped", defaultBoolean = false)
    public void setRecenterIfGraphicTapped(RNAGSMapView view, Boolean value){
        view.setRecenterIfGraphicTapped(value);
    }

        @ReactProp(name = "maximumResult",defaultInt=1)
    public void setValueMaximumResult(RNAGSMapView view, Integer value){
        view.setValueMaximumResult(value);
    }

    @ReactProp(name= "minZoom")
    public void setMinZoom(RNAGSMapView view, double value) {
        view.setMinZoom(value);
    }

    @ReactProp(name= "maxZoom")
    public void setMaxZoom(RNAGSMapView view, double value) {
        view.setMaxZoom(value);
    }

    @ReactProp(name= "rotationEnabled")
    public void setRotationEnabled(RNAGSMapView view, Boolean value) {
        view.setRotationEnabled(value);
    }


    // MARK: RN Methods
    @Override
    public Map<String, Integer> getCommandsMap() {
        Map<String, Integer> map = MapBuilder.of(
                "showCalloutViaManager",SHOW_CALLOUT,
                "centerMapViaManager", CENTER_MAP,
                "addGraphicsOverlayViaManager", ADD_GRAPHICS_OVERLAY,
                "removeGraphicsOverlayViaManager",REMOVE_GRAPHICS_OVERLAY,
                "addPointsToOverlayViaManager",ADD_POINTS_TO_OVERLAY,
                "removePointsFromOverlayViaManager", REMOVE_POINTS_FROM_OVERLAY,
                "updatePointsInGraphicsOverlayViaManager", UPDATE_POINTS_IN_GRAPHICS_OVERLAY
        );
        // Ran out of space in the constructor lol
        map.put("routeGraphicsOverlayViaManager",ROUTE_GRAPHICS_OVERLAY);
        map.put("setRouteIsVisible", SET_ROUTE_IS_VISIBLE);
        map.put("dispose", DISPOSE);
        map.put("reloadMap", RE_LOAD_MAP);

      return map;
    }

    @Override
    public void receiveCommand(RNAGSMapView mapView, int command, ReadableArray args) {
        Assertions.assertNotNull(mapView);
        Assertions.assertNotNull(args);
        switch (command) {
            case SHOW_CALLOUT: mapView.showCallout(args.getMap(0));return;
            case CENTER_MAP: mapView.centerMap(args.getArray(0));return;
            case ADD_GRAPHICS_OVERLAY: mapView.addGraphicsOverlay(args.getMap(0));return;
            case REMOVE_GRAPHICS_OVERLAY: mapView.removeGraphicsOverlay(args.getString(0));return;
            case ADD_POINTS_TO_OVERLAY: mapView.addPointsToOverlay(args.getMap(0));return;
            case REMOVE_POINTS_FROM_OVERLAY: mapView.removePointsFromOverlay(args.getMap(0));return;
            case UPDATE_POINTS_IN_GRAPHICS_OVERLAY: mapView.updatePointsInGraphicsOverlay(args.getMap(0));return;
            case ROUTE_GRAPHICS_OVERLAY: mapView.routeGraphicsOverlay(args.getMap(0));return;
            case SET_ROUTE_IS_VISIBLE: mapView.setRouteIsVisible(args.getBoolean(0));return;
            case RE_LOAD_MAP:mapView.reLoadMap();return;
            case DISPOSE: mapView.onHostDestroy();
        }
    }

    // MARK: Event receiving
    public Map getExportedCustomBubblingEventTypeConstants() {
        return MapBuilder.builder()
                        .put(
                        "onLocationChanged",
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onLocationChanged")))
                .put(
                        "onSingleTap",
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onSingleTap")))
                .put(
                        "onMapDidLoad",
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onMapDidLoad")))
                .put(
                        "onOverlayWasModified",
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onOverlayWasModified")))
                .put(
                        "onOverlayWasAdded",
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onOverlayWasAdded")))
                .put(
                        "onOverlayWasRemoved",
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onOverlayWasRemoved")))
                .put(
                        "onMapMoved",
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onMapMoved")))
                .build();
    }
}
