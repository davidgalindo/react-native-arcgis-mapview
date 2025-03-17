//  Created by react-native-create-bridge

import React from "react";
import {
  NativeEventEmitter,
  Platform,
  requireNativeComponent,
  NativeModules,
  UIManager,
  findNodeHandle,
  DeviceEventEmitter,
} from "react-native";
import PropTypes from "prop-types";
const AGSMap = requireNativeComponent("RNArcGISMapView", ArcGISMapView);

class ArcGISMapView extends React.Component {
  constructor(props) {
    super(props);
    var eventEmitter;
    if (Platform.OS === "ios") {
      eventEmitter = new NativeEventEmitter(
        NativeModules.RNArcGISMapViewModule
      );
    } else {
      eventEmitter = DeviceEventEmitter;
    }
    eventEmitter.addListener(
      "isRoutingChanged",
      this.props.onRoutingStatusUpdate
    );
  }

  // MARK: Props
  static propTypes = {
    basemapUrl: PropTypes.string,
    initialMapCenter: PropTypes.shape({
      points: PropTypes.arrayOf(PropTypes.object),
      stroke: PropTypes.number,
      scaleIos: PropTypes.number,
      scaleAndroid: PropTypes.number,
    }),
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
    enableCurrentLocation: PropTypes.bool,
  };

  static defaultProps = {
    initialMapCenter: {
      points: [{ latitude: 36.244797, longitude: -94.14806 }],
      stroke: 1.0,
      mapScale: 0.5,
    },
    minZoom: 0,
    maxZoom: 0,
    rotationEnabled: true,
    basemapUrl: "",
    onSingleTap: () => {},
    onOverlayWasAdded: () => {},
    onOverlayWasRemoved: () => {},
    onOverlayWasModified: () => {},
    onMapDidLoad: () => {},
    onMapMoved: () => {},
    onRoutingStatusUpdate: () => {},
    routeUrl: "",
    enableCurrentLocation: true,
  };

  isRouting = false;

  // MARK: Exposed native methods
  showCallout = (args) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig("RNArcGISMapView").Commands
        .showCalloutViaManager,
      [args]
    );
  };

  recenterMap = (pointArray) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig("RNArcGISMapView").Commands
        .centerMapViaManager,
      [pointArray]
    );
  };

  addLocation = (pointArray) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig("RNArcGISMapView").Commands
        .addCurrentLocationViaManager,
      [pointArray]
    );
  };

  addSketchToMap = (pointArray) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig("RNArcGISMapView").Commands
        .addSketchToMapViaManager,
      [pointArray]
    );
  };

  stopSketchOnMap = () => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig("RNArcGISMapView").Commands
        .stopSketchToMapViaManager,
      []
    );
  };

  stopSketchDrawOverlay = () => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig("RNArcGISMapView").Commands
        .stopSketchDrawOverlayManager,
      []
    );
  };

  addGraphicsOverlay = (overlayData) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig("RNArcGISMapView").Commands
        .addGraphicsOverlayViaManager,
      [overlayData]
    );
  };

  removeGraphicsOverlay = (overlayId) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig("RNArcGISMapView").Commands
        .removeGraphicsOverlayViaManager,
      [overlayId]
    );
  };

  addPointsToOverlay = (args) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig("RNArcGISMapView").Commands
        .addPointsToOverlayViaManager,
      [args]
    );
  };
  
  updatePointsOnOverlay = (args) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig("RNArcGISMapView").Commands
        .updatePointsInGraphicsOverlayViaManager,
      [args]
    );
  };

  removePointsFromOverlay = (args) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig("RNArcGISMapView").Commands
        .removePointsFromOverlayViaManager,
      [args]
    );
  };

  routeGraphicsOverlay = (args) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig("RNArcGISMapView").Commands
        .routeGraphicsOverlayViaManager,
      [args]
    );
  };

  getRouteIsVisible = (callback) => {
    if (Platform.OS === "ios") {
      UIManager.dispatchViewManagerCommand(
        findNodeHandle(this.agsMapRef),
        UIManager.getViewManagerConfig("RNArcGISMapView").Commands
          .getRouteIsVisibleViaManager,
        [callback]
      );
    } else {
      NativeModules.RNArcGISMapViewManager.getRouteIsVisible(
        findNodeHandle(this.agsMapRef),
        callback
      );
    }
  };

  setRouteIsVisible = (args) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig("RNArcGISMapView").Commands
        .setRouteIsVisibleViaManager,
      [args]
    );
  };
  reloadMap = () => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig("RNArcGISMapView").Commands.reloadMap,
      []
    );
  };

  // MARK: Render
  render() {
    return <AGSMap {...this.props} ref={(e) => (this.agsMapRef = e)} />;
  }

  // MARK: Disposal
  componentWillUnmount() {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig("RNArcGISMapView").Commands.dispose,
      []
    );
  }
}

export const setLicenseKey = (string) => {
  NativeModules.RNArcGISMapViewManager.setLicenseKey(string);
};

export const setApiKey = (string) => {
  NativeModules.RNArcGISMapViewManager.setApiKey(string);
};

export default ArcGISMapView;
