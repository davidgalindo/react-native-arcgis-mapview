//  Created by react-native-create-bridge

import React from 'react'
import { requireNativeComponent,  UIManager, findNodeHandle } from 'react-native'
import PropTypes from 'prop-types'
const AGSMap = requireNativeComponent('RNArcGISMapView', ArcGISMapView);

class ArcGISMapView extends React.Component {
  // MARK: Props
  static propTypes = {
    basemapUrl: PropTypes.string,
    initialMapCenter: PropTypes.arrayOf(PropTypes.object),
    onSingleTap: PropTypes.func,
  };

  static defaultProps = {
    initialMapCenter: [
      {latitude: 36.244797, longitude: -94.148060}
    ],
    basemapUrl: '',
    onSingleTap: (event) => { }

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

export default ArcGISMapView;