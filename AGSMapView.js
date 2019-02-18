//  Created by react-native-create-bridge

import React from 'react'
import { requireNativeComponent, NativeModules,  UIManager, findNodeHandle } from 'react-native'
import PropTypes from 'prop-types'
const AGSMap = requireNativeComponent('RNArcGISMapView', ArcGISMapView);


class ArcGISMapView extends React.Component {
  // MARK: Props
  static propTypes = {
    basemapUrl: PropTypes.string,
    initialMapCenter: PropTypes.arrayOf(PropTypes.object),
    onSingleTap: PropTypes.func,
    routeUrl: PropTypes.string,
  };

  static defaultProps = {
    initialMapCenter: [
      {latitude: 36.244797, longitude: -94.148060}
    ],
    basemapUrl: '',
    onSingleTap: (event) => { },
    routeUrl: '',
  };

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
      NativeModules.RNArcGISMapViewManager.getRouteIsVisible(findNodeHandle(this.agsMapRef),callback);
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
  render () {
    return <AGSMap {...this.props} ref={e => this.agsMapRef = e} />
  }

  // MARK: Disposal
  componentWillUnmount() {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.agsMapRef),
      UIManager.getViewManagerConfig('RNArcGISMapView').Commands.dispose,
      [args]
    );
  }
}

export const setLicenseKey = (string) => {
  NativeModules.RNArcGISMapViewManager.setLicenseKey(string);
};


export default ArcGISMapView;