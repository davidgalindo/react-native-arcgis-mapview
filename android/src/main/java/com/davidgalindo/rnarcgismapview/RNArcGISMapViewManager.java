package com.davidgalindo.rnarcgismapview;
import android.app.Activity;
import android.support.annotation.Nullable;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.ReadableArray;
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
    public void setInitialMapCenter(RNAGSMapView view, @Nullable ReadableArray array) {
        view.setInitialMapCenter(array);
    }

    @ReactProp(name = "recenterIfGraphicTapped", defaultBoolean = false)
    public void setRecenterIfGraphicTapped(RNAGSMapView view, Boolean value){
        view.setRecenterIfGraphicTapped(value);
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
        map.put("dispose", DISPOSE);
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
            case DISPOSE: mapView.onHostDestroy();
        }
    }

    // MARK: Event receiving
    public Map getExportedCustomBubblingEventTypeConstants() {
        return MapBuilder.builder()
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
                .build();
    }
}
