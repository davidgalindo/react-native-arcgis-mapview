//
//  RNArcGISMapViewManager.swift
//  SampleArcGIS
//
//  Created by David Galindo on 1/31/19.
//  Copyright © 2019 Facebook. All rights reserved.
//

import Foundation

@objc(RNArcGISMapViewManager)
public class RNArcGISMapViewManager: RCTViewManager {
  var agsMapView: RNArcGISMapView?
  
  override public func view() -> UIView! {
    if (agsMapView == nil) {
      agsMapView = RNArcGISMapView()
    }
    return agsMapView!
  }
  
  override public class func requiresMainQueueSetup() -> Bool {
    return true;
  }
  
  // MARK: Exposed Obj-C bridging functions
  @objc func showCalloutViaManager(_ node: NSNumber, args: NSDictionary) {
    DispatchQueue.main.async {
      let component = self.bridge.uiManager.view(forReactTag: node) as! RNArcGISMapView
      component.showCallout(args)
    }
  }
  
  @objc func centerMapViaManager(_ node: NSNumber, args: NSArray) {
    DispatchQueue.main.async {
      let component = self.bridge.uiManager.view(forReactTag: node) as! RNArcGISMapView
      component.centerMap(args)
    }
  }
  
  @objc func addGraphicsOverlayViaManager(_ node: NSNumber, args: NSDictionary) {
    DispatchQueue.main.async {
      let component = self.bridge.uiManager.view(forReactTag: node) as! RNArcGISMapView
      component.addGraphicsOverlay(args)
    }
  }
  
  @objc func addPointsToOverlayViaManager(_ node: NSNumber, args: NSDictionary) {
    DispatchQueue.main.async {
      let component = self.bridge.uiManager.view(forReactTag: node) as! RNArcGISMapView
      component.addPointsToGraphicsOverlay(args)
    }
  }
  
  @objc func removePointsFromOverlayViaManager(_ node: NSNumber, args: NSDictionary) {
    DispatchQueue.main.async {
      let component = self.bridge.uiManager.view(forReactTag: node) as! RNArcGISMapView
      component.removePointsFromGraphicsOverlay(args)
    }
  }
  
  @objc func removeGraphicsOverlayViaManager(_ node: NSNumber, args: NSString) {
    DispatchQueue.main.async {
      let component = self.bridge.uiManager.view(forReactTag: node) as! RNArcGISMapView
      component.removeGraphicsOverlay(args)
    }
  }
  
  @objc func updatePointsInGraphicsOverlayViaManager(_ node: NSNumber, args: NSDictionary) {
    DispatchQueue.main.async {
      let component = self.bridge.uiManager.view(forReactTag: node) as! RNArcGISMapView
      component.updatePointsInGraphicsOverlay(args)
    }
  }
  
  @objc func dispose(_ node: NSNumber) {
    self.agsMapView?.graphicsOverlays.removeAllObjects()
    self.agsMapView?.map = nil
    self.agsMapView = nil
  }
  
}
