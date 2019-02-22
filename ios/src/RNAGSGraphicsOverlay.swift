//
//  RNAGSGraphicsLayer.swift
//  SampleArcGIS
//
//  Created by David Galindo on 1/31/19.
//  Copyright © 2019 David Galindo. All rights reserved.
//  Bridge between React Native and ArcGIS to create feature layers.
//  This helper class is for static, nonchanging data. It does not (yet)
//  support adding or removing points.
//
//

import Foundation
import ArcGIS

public class RNAGSGraphicsOverlay: AGSGraphicsOverlay {
    var pointImageDictionary: [NSString: UIImage]
    let referenceId: NSString
    var shouldAnimateUpdate: Bool = false
    
    // MARK: Initializer
    init(rawData: NSDictionary){
        guard let referenceIdRaw = rawData["referenceId"] as? NSString else {
            fatalError("The RNAGSGraphicsLayer needs a reference ID.")
        }
        guard let rawDataCasted = rawData["points"] as? [NSDictionary] else {
            fatalError("The RNAGSGraphicsLayer recieved invalid point data: \(rawData)")
        }
        referenceId = referenceIdRaw
        pointImageDictionary = [:]
        if let pointImagesRaw = rawData["pointGraphics"] as? [NSDictionary] {
            for item in pointImagesRaw {
                if let graphicId = item["graphicId"] as? NSString, let graphic = RCTConvert.uiImage(item["graphic"]) {
                    pointImageDictionary[graphicId] = graphic
                }
            }
        }
        super.init()
        for item in rawDataCasted {
            if let point = RNAGSGraphicsOverlay.createPoint(rawData: item) {
                let agsGraphic = RNAGSGraphicsOverlay.rnPointToAGSGraphic(point, pointImageDictionary: pointImageDictionary)
                self.graphics.add(agsGraphic)
            }
        }
    }
    
    func updateGraphic(with args: NSDictionary) {
        // First, find the graphic with the reference ID
        guard let referenceId = args["referenceId"] as? NSString else {
            return
        }
        // Look for graphic within graphics
        guard let graphic = self.graphics.first(where: { (item) -> Bool in
            return (item as! AGSGraphic).attributes["referenceId"] as! NSString == referenceId
        }) as? AGSGraphic else {
            // No result found, nothing to update
            return
        }
        // From here, we check for each attribute individually
        let latitude = args["latitude"] as? NSNumber
        let longitude = args["longitude"] as? NSNumber
        let originalPosition = graphic.geometry as! AGSPoint
        let attributes = args["attributes"] as? [NSString: Any]
        let rotation = args["rotation"] as? NSNumber
        let rawLocationData = CLLocationCoordinate2D(latitude: latitude?.doubleValue ?? originalPosition.x, longitude: longitude?.doubleValue ?? originalPosition.y)
        let graphicPoint = AGSPoint(clLocationCoordinate2D: rawLocationData)
        
        // Once we have all the possible update values, we change them
        if let graphicId = args["graphicId"] as? NSString, let newImage = pointImageDictionary[graphicId] {
            let symbol = AGSPictureMarkerSymbol(image: newImage)
            // update location and graphic
            graphic.symbol = symbol
            
        }
        // Update geometry here
        let fromPoint = graphic.geometry as! AGSPoint
        let fromRotation = (graphic.symbol as? AGSPictureMarkerSymbol)?.angle
        if (shouldAnimateUpdate) {
            update(graphic: graphic, fromPoint: fromPoint, toPoint: graphicPoint, fromRotation: fromRotation ?? 0, toRotation: rotation?.floatValue ?? 0)
        } else {
            // Update rotation and geometry without animation
            graphic.geometry = graphicPoint
            if let rotation = rotation?.floatValue {
                (graphic.symbol as? AGSPictureMarkerSymbol)?.angle = rotation
            }
        }
        // Attributes
        if let attributes = attributes {
            graphic.attributes.addEntries(from: attributes)
        }
        // End of updates
        
    }
    
    let timerDuration: Double = 0.5
    var timer = Timer()
    private static let timerFireMax:NSNumber = 10.0
    // Here we will animate the movement of a point - both position and angle
    private func update(graphic: AGSGraphic, fromPoint: AGSPoint, toPoint:AGSPoint, fromRotation: Float, toRotation: Float){
        let dx = (toPoint.x - fromPoint.x) / RNAGSGraphicsOverlay.timerFireMax.doubleValue
        let dy = (toPoint.y - fromPoint.y) / RNAGSGraphicsOverlay.timerFireMax.doubleValue
        let dTheta = (toRotation - fromRotation) / RNAGSGraphicsOverlay.timerFireMax.floatValue
        let symbol = graphic.symbol as? AGSPictureMarkerSymbol
        var timesFired = 0.0
        Timer.scheduledTimer(withTimeInterval: timerDuration / RNAGSGraphicsOverlay.timerFireMax.doubleValue, repeats: true, block: {
            if (timesFired < RNAGSGraphicsOverlay.timerFireMax.doubleValue) {
                let x = fromPoint.x + (dx * timesFired)
                let y = fromPoint.y + (dy * timesFired)
                let rotation = Double(fromRotation) + (Double(dTheta) * timesFired)
                graphic.geometry = AGSPoint(x: x, y: y, spatialReference: AGSSpatialReference.wgs84())
                symbol?.angle = Float(rotation)
                timesFired += 1.0
            } else {
                graphic.geometry = toPoint
                symbol?.angle = toRotation
                $0.invalidate()
            }
        })
    }
    
    
    
    
    // MARK: Static methods
    public static func rnPointToAGSGraphic(_ point: Point, pointImageDictionary: [NSString: UIImage]?) -> AGSGraphic{
        let graphicPoint = CLLocationCoordinate2D(latitude: point.latitude.doubleValue, longitude: point.longitude.doubleValue)
        let agsPoint = AGSPoint(clLocationCoordinate2D: graphicPoint)
        let agsGraphic: AGSGraphic
        if let imageId = point.imageId, let image = pointImageDictionary?[imageId] {
            let symbol = AGSPictureMarkerSymbol(image: image)
            agsGraphic = AGSGraphic(geometry: agsPoint, symbol: symbol, attributes: point.attributes)
        } else {
            let symbol = AGSSimpleMarkerSymbol(style: .circle, color: UIColor.green, size: 10)
            agsGraphic = AGSGraphic(geometry: agsPoint, symbol: symbol, attributes: point.attributes)
        }
        agsGraphic.attributes["referenceId"] = point.referenceId
        return agsGraphic
    }
    
    public static func createPoint(rawData: NSDictionary) -> Point?{
        // Verify all required values are available
        if let latitude = rawData["latitude"] as? NSNumber,
            let longitude = rawData["longitude"] as? NSNumber,
            let referenceId = rawData["referenceId"] as? NSString{
            // Initialize any optional values
            let rotation = rawData["rotation"] as? NSNumber ?? 0
            let attributes = RNAGSGraphicsOverlay.convert(nsKeyedDictionary: rawData["properties"] as? [NSString: Any])
            let imageId = rawData["graphicId"] as? NSString
            return RNAGSGraphicsOverlay.Point(_latitude: latitude,
                                              _longitude: longitude,
                                              _rotation: rotation,
                                              _attributes: attributes,
                                              _referenceId: referenceId,
                                              _imageId: imageId
            )
        } else {
            return nil
        }
    }
    
    private static func convert(nsKeyedDictionary: [NSString: Any]?) -> [String: Any]? {
        guard let nsKeyedDictionary = nsKeyedDictionary else {
            return nil
        }
        var result:[String:Any] = [:]
        for key in nsKeyedDictionary.keys {
            result[String(key)] = nsKeyedDictionary[key]
        }
        return result
    }
    
    // MARK: Inner class
    public class Point {
        let latitude: NSNumber
        let longitude: NSNumber
        let rotation: NSNumber
        let attributes: [String: Any]?
        let referenceId: NSString
        let imageId: NSString?
        init(_latitude: NSNumber, _longitude: NSNumber, _rotation: NSNumber, _attributes: [String: Any]?, _referenceId: NSString, _imageId: NSString?) {
            self.latitude = _latitude
            self.longitude = _longitude
            self.rotation = _rotation
            self.attributes = _attributes
            self.referenceId = _referenceId
            self.imageId = _imageId
        }
    }
}
