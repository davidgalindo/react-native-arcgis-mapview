/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 * @lint-ignore-every XPLATJSCOPYRIGHT1
 */

import React, {Component} from 'react';
import {Image, StyleSheet, Text, View, TouchableOpacity} from 'react-native';
import ArcGISMapView  from 'react-native-arcgis-mapview';
import * as Updater from './updateSample';

export default class App extends Component {
  constructor(props) {
    super(props);
    this.state = {
      lastX: 0.0,
      lastY: 0.0,
      isOverlayVisible: true,
      updatesActive: false,
      graphicId: 'none',
    }
  }

  render() {

    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>React Native ArcGIS</Text>
        <TouchableOpacity onPress={this.onCalloutPress}>
        <Text style={styles.instructions}>Show callout at JB Hunt (iOS Only)</Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={this.onMultiRecenterPress}>
          <Text style={styles.instructions}>Recenter around LA and NY</Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={this.onOverlayPress}>
          <Text style={styles.instructions}>Toggle Graphics Overlay</Text>
        </TouchableOpacity>
        <View style={{flexDirection: 'row'}}> 
        <TouchableOpacity disabled={this.state.isOverlayVisible} onPress={this.onPointAddPress}> 
          <Text style={styles.positiveButton}>Add points  </Text>
        </TouchableOpacity>
        <TouchableOpacity disabled={this.state.isOverlayVisible} onPress={this.onPointRemovePress}> 
          <Text style={styles.negativeButton}>  Remove points </Text>
        </TouchableOpacity>
        <TouchableOpacity disabled={this.state.isOverlayVisible} onPress={this.onPointUpdatePress}> 
          <Text style={styles.triggerButton}>  Toggle moving point</Text>
        </TouchableOpacity>
        </View>
        <View style={{flexDirection: 'row'}}>
        
        </View>
        <ArcGISMapView ref={mapView => this.mapView = mapView} style={{width: 360, height: 500}}
          initialMapCenter={[{latitude: 36.244797, longitude: -94.148060}]}
          recenterIfGraphicTapped={true}
          basemapUrl='https://david-galindo.maps.arcgis.com/home/item.html?id=fc75f65db9504175b2fb0e87b66672e5'
          onSingleTap={this.onSingleTap}
      />
      <Text style={styles.tapTracker}> X: <Text style={{fontWeight: 'bold'}}>{this.state.lastX}</Text> Y: <Text style={{fontWeight: 'bold'}}>{this.state.lastY}</Text> Graphic ID: <Text style={{fontWeight: 'bold'}}>{this.state.graphicId}</Text> </Text>
      </View>
    );
  }

  // MARK: Map actions
  onCalloutPress = () => {
    if (this.mapView) {
      this.mapView.showCallout({
        point: {
          latitude: 36.244797, longitude: -94.148060
        },
        title: 'Callout',
        text: 'I work here!',
        shouldRecenter: true,
      }); 
    }
  }

  onMultiRecenterPress = () => {
    if(this.mapView) {
      this.mapView.recenterMap([
        {latitude: 34.052235, longitude: -118.243683 }, // Los Angeles, CA <3
        {latitude: 40.712776, longitude: -74.005974}, // New York, NY
      ]);
    }
  }

  onOverlayPress = () => {
    if (this.mapView) {
      if (this.state.isOverlayVisible){
        this.mapView.addGraphicsOverlay(this.overlayData);
      } else {
        this.mapView.removeGraphicsOverlay('graphicsOverlay');
        Updater.endUpdate(this);
        this.setState({ updatesActive: false });
      }
      this.setState({isOverlayVisible: !this.state.isOverlayVisible})
    }
  }

  onPointAddPress = () => {
    if (this.mapView) {
      this.mapView.addPointsToOverlay({
        overlayReferenceId: 'graphicsOverlay',
        points:[{
          latitude: 44.953705,
          longitude: -93.089958,
          rotation: 0,
          referenceId: 'St. Paul',
          graphicId: 'normalPoint',
        },{
          latitude: 35.084385,
          longitude: -106.650421,
          rotation: 0,
          referenceId: 'Albuquerque',
          graphicId: 'personPoint',
        },
      ],
      pointGraphics: this.pointGraphics,
      });
    }
  }

  onPointRemovePress = () => {
    if (this.mapView) {
      this.mapView.removePointsFromOverlay({
        overlayReferenceId: 'graphicsOverlay',
        referenceIds: [
          'St. Paul',
          'Albuquerque'
        ]
      })
    }
  };

  onPointUpdatePress = () => {
    if (this.state.updatesActive) {
      // Disable updates
      Updater.endUpdate();
    } else {
      Updater.beginUpdate(this, (args) => {
        if (this.mapView) {
          this.mapView.updatePointsOnOverlay(args)
        }
      });
    }
    this.setState({updatesActive: !this.state.updatesActive});
  };

  onSingleTap = (event) =>{
    points = event.nativeEvent;
    if (!points.mapPoint) {
      return;
    }
    if (points.mapPoint.latitude && points.mapPoint.longitude) {
      this.setState({lastX: points.mapPoint.latitude.toFixed(4), lastY: points.mapPoint.longitude.toFixed(4)})
    }
    if (points.graphicReferenceId) {
      this.setState({ graphicId: points.graphicReferenceId});
    } else {
      this.setState({ graphicId: 'none'});
    }
  };

  // MARK: Mock data
  pointGraphics = [
    {graphicId: 'normalPoint', graphic: Image.resolveAssetSource(require('./src/normalpoint.png'))},
    {graphicId: 'personPoint', graphic: Image.resolveAssetSource(require('./src/personpoint.png'))},
    {graphicId: 'planePoint', graphic: Image.resolveAssetSource(require('./src/planepoint.png'))},
  ];

  overlayData = {
    pointGraphics: this.pointGraphics,
    referenceId: 'graphicsOverlay',
    points: [{
      latitude: 45.512230,
      longitude: -122.658722,
      rotation: 0,
      referenceId: 'Portland',
      graphicId: 'normalPoint',
    },{
      latitude: 38.907192,
      longitude: -77.036873,
      rotation: 0,
      referenceId: 'Washington, D.C.',
      graphicId: 'personPoint',
    },{
      latitude: 39.739235,
      longitude: -104.990250,
      rotation: 0,
      referenceId: 'movingImage',
      graphicId: 'planePoint',
    },
  ]
  };


} // End component

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 5,
  },
  instructions: {
    textAlign: 'center',
    color: '#b375f4',
    fontWeight: 'bold',
    marginBottom: 5,
  },
  positiveButton: {
    color: '#36bc3f',
    fontWeight: 'bold',
  },
  negativeButton: {
    color: '#bc3667',
    fontWeight: 'bold',
  },
  triggerButton: {
    color: '#36a5bc',
    fontWeight: 'bold',
  },
  tapTracker: {
    textAlign: 'center',
    color: '#777777',
    marginBottom: 5,
  },
});
