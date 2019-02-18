//
//  RNAGSMapViewModule.swift
//  RNArcGISMapView
//
//  Created by David Galindo on 2/18/19.
//  Copyright © 2019 David Galindo. All rights reserved.
//

import ArcGIS
import Foundation

@objc(RNAGSMapViewModule)
public class RNArcGISMapViewModule: NSObject{
    @objc func setLicenseKey(_ key: String) {
        do {
            try AGSArcGISRuntimeEnvironment.setLicenseKey("key")
        }
        catch let error as NSError {
            print("error: \(error)")
        }
    }
}
