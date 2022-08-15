package com.davidgalindo.rnarcgismapview;

import androidx.annotation.Nullable;

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
    private final int SET_ROUTE_IS_VISIBLE = 9;
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
                .put(
                        "onMapMoved",
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onMapMoved")))
                .build();
    }
}
//  Created by react-native-create-bridge

import React from 'react'
import { NativeEventEmitter, Platform, requireNativeComponent, NativeModules, UIManager, findNodeHandle, DeviceEventEmitter } from 'react-native'
import PropTypes from 'prop-types'
const AGSMap = requireNativeComponent('RNArcGISMapView', ArcGISMapView);


class ArcGISMapView extends React.Component {
  constructor(props) {
    super(props);
    var eventEmitter;
    if (Platform.OS === 'ios') {
      eventEmitter = new NativeEventEmitter(NativeModules.RNArcGISMapViewModule);
    } else {
      eventEmitter = DeviceEventEmitter;
    }
    eventEmitter.addListener('isRoutingChanged', this.props.onRoutingStatusUpdate);
  }

  // MARK: Props
  static propTypes = {
    basemapUrl: PropTypes.string,
    initialMapCenter: PropTypes.arrayOf(PropTypes.object),
    minZoom: PropTypes.number,
    maxZoom: PropTypes.number,
    rotationEnabled: PropTypes.bool,
    routeUrl: PropTypes.string,
    onOverlayWasAdded: PropTypes.func,
    onOverlayWasRemoved: PropTypes.func,
    onOverlayWasModified: PropTypes.func,
    onMapDidLoad: PropTypes.func,
    onMapMoved: PropTypes.func,
    onSingleTap: PropTypes.func,
    maximumResult: PropTypes.number,
    enableCurrentLocation: PropTypes.bool
  };

  static defaultProps = {
    initialMapCenter: [
      { latitude: 36.244797, longitude: -94.148060 }
    ],
    minZoom: 0,
    maxZoom: 0,
    rotationEnabled: true,
    basemapUrl: '',
    onSingleTap: () => { },
    onOverlayWasAdded: () => { },
    onOverlayWasRemoved: () => { },
    onOverlayWasModified: () => { },
    onMapDidLoad: () => { },
    onMapMoved: () => { },
    onRoutingStatusUpdate: () => { },
    routeUrl: '',
    enableCurrentLocation: true,
  };

  isRouting = false;

  // MARK: Exposed native methods
  showCallout = (args) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig('RNArcGISMapView').Commands.showCalloutViaManager,
      [args]
    );
  };

  recenterMap = (pointArray) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig('RNArcGISMapView').Commands.centerMapViaManager,
      [pointArray]
    );
  }

  addLocation = (pointArray) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig('RNArcGISMapView').Commands.addCurrentLocationViaManager,
      [pointArray]
    );
  }

  addSketchToMap = (pointArray) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig('RNArcGISMapView').Commands.addSketchToMapViaManager,
      [pointArray]
    );
  }

  stopSketchOnMap = () => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig('RNArcGISMapView').Commands.stopSketchToMapViaManager,
      []
    );
  }

  stopSketchDrawOverlay = () => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig('RNArcGISMapView').Commands.stopSketchDrawOverlayManager,
      []
    );
  }

  addGraphicsOverlay = (overlayData) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig('RNArcGISMapView').Commands.addGraphicsOverlayViaManager,
      [overlayData]
    );
  }

  removeGraphicsOverlay = (overlayId) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig('RNArcGISMapView').Commands.removeGraphicsOverlayViaManager,
      [overlayId]
    );
  }

  addPointsToOverlay = (args) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig('RNArcGISMapView').Commands.addPointsToOverlayViaManager,
      [args]
    );
  }

  updatePointsOnOverlay = (args) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig('RNArcGISMapView').Commands.updatePointsInGraphicsOverlayViaManager,
      [args]
    );
  }

  removePointsFromOverlay = (args) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig('RNArcGISMapView').Commands.removePointsFromOverlayViaManager,
      [args]
    );
  }

  routeGraphicsOverlay = (args) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig('RNArcGISMapView').Commands.routeGraphicsOverlayViaManager,
      [args]
    );
  }

  getRouteIsVisible = (callback) => {
    if (Platform.OS === 'ios') {
      UIManager.dispatchViewManagerCommand(
        findNodeHandle(this.agsMapRef),
        UIManager.getViewManagerConfig('RNArcGISMapView').Commands.getRouteIsVisibleViaManager,
        [callback]
      );
    } else {
      NativeModules.RNArcGISMapViewManager.getRouteIsVisible(findNodeHandle(this.agsMapRef), callback);
    }
  };

  setRouteIsVisible = (args) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig('RNArcGISMapView').Commands.setRouteIsVisibleViaManager,
      [args]
    );
  }

  // MARK: Render
  render() {
    return <AGSMap {...this.props} ref={e => this.agsMapRef = e} />
  }

  // MARK: Disposal
  componentWillUnmount() {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig('RNArcGISMapView').Commands.dispose,
      []
    );
  }
}

export const setLicenseKey = (string) => {
  NativeModules.RNArcGISMapViewManager.setLicenseKey(string);
};

export const setApiKey = (string) => {
  NativeModules.RNArcGISMapViewManager.setApiKey(string);
}


export default ArcGISMapView;
