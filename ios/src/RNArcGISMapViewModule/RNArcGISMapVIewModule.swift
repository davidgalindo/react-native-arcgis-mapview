//
//  RNArcGISMapVIewModule.swift
//  RNArcGISMapView
//
//  Created by David Galindo on 2/20/19.
//  Copyright © 2019 Facebook. All rights reserved.
//

import Foundation

@objc(RNArcGISMapViewModule)
public class RNArcGISMapViewModule {
    
    // MARK: Event emitting to JS
    @objc func sendIsRoutingChanged(_ value: Bool) {
        sendEvent(withName: "isRoutingChanged", body: [value])
    }
    
    
    // MARK: Overrides
    
    func supportedEvents() -> [String]! {
        return ["isRoutingChanged"]
    }
    
    static func requirersMainQueueSetup() {
        return false
    }
    
    func constantsToExport() -> [AnyHashable : Any]! {
        return [:]
    }
    
}
