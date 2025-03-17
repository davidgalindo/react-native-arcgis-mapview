//
//  RNArcGISMapView.swift
//  SampleArcGIS
//
//  Created by David Galindo on 1/31/19.
//  Copyright © 2019 David Galindo. All rights reserved.
//

import UIKit
import ArcGIS

@objc(RNArcGISMapView)
public class RNArcGISMapView: AGSMapView, AGSGeoViewTouchDelegate {
    // MARK: Properties
    var routeGraphicsOverlay = AGSGraphicsOverlay()
    var router: RNAGSRouter?
    var bridge: RCTBridge?
    var initialCenterPoint: NSDictionary?
    // MARK: Initializers and helper methods
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        setUpMap()
    }
    override init(frame: CGRect) {
        super.init(frame: frame)
        setUpMap()
    }

    func setUpMap() {
        // Default is to Esri HQ
        self.map  =   AGSMap(basemapStyle: .arcGISTopographic)
        // self.map = AGSMap(basemapType: .topographic, latitude: 34.057, longitude: -117.196, levelOfDetail: 17)
        self.map?.load(completion: {[weak self] (error) in
            if (self?.onMapDidLoad != nil){
                var reactResult: [AnyHashable: Any] = ["success" : error != nil]
                if (error != nil) {
                    reactResult["errorMessage"] = error!.localizedDescription
                }
                self?.onMapDidLoad!(reactResult)
            }
        })
        // hide attribution text for map view
        self.isAttributionTextVisible = false

        self.backgroundGrid?.gridLineWidth=0
        self.backgroundGrid?.gridSize=0
        self.backgroundGrid?.color = .white

        self.touchDelegate = self
        self.graphicsOverlays.add(routeGraphicsOverlay)
        //display current device location

        // var colorLocation = hexStringToUIColor(hex: "#B71D21")
        // let lineSymbol = AGSSimpleLineSymbol(style: .solid, color: .white, width: 2.0)
        // let startingPointSymbol = AGSSimpleMarkerSymbol(style: .circle, color:colorLocation , size: 18 )
        // startingPointSymbol.outline=lineSymbol

        //get image by asset
        if  let locationSymbol =  UIImage(named:  "locationcurrent"){
            let markerSymbol = AGSPictureMarkerSymbol(image: locationSymbol)
            self.locationDisplay.defaultSymbol=markerSymbol
            self.locationDisplay.acquiringSymbol=markerSymbol
        }
        self.locationDisplay.start {[weak self] error in
            guard let self = self else {return}
            if let error = error {
                print(error.localizedDescription)
                return
            }
            print( "display location success" )
            self.locationDisplay.autoPanMode = .off

        }
    }

    private func startProcessingLocationChanges() {
        self.locationDisplay.locationChangedHandler = { [weak self] (location) in
            guard location.horizontalAccuracy >= 0 else { return }
            DispatchQueue.main.async {
                self?.processLocationUpdate()
            }
        }
    }

    private func processLocationUpdate() {
        guard let position = self.locationDisplay.mapLocation, position.x != 0, position.y != 0 else { return }
        //position
            var reactResult: [AnyHashable: Any] = [
                "mapPoint": ["lat" : position.y, "lng": position.x],
            ]

             if onLocationChanged != nil {
          self.onLocationChanged!(reactResult)
             }
          print( "location device changed  : \(position)" )
    }

    // MARK: Native methods
    @objc func geoView(_ geoView: AGSGeoView, didTapAtScreenPoint screenPoint: CGPoint, mapPoint: AGSPoint) {
        self.callout.dismiss()
        // canMoveViewshed = true
        if onSingleTap != nil {
            let latLongPoint = AGSGeometryEngine.projectGeometry(mapPoint, to: AGSSpatialReference.wgs84()) as! AGSPoint
            var reactResult: [AnyHashable: Any] = [
                "mapPoint": ["latitude" : latLongPoint.y, "longitude": latLongPoint.x],
                "screenPoint" : ["x": screenPoint.x, "y": screenPoint.y]
            ]
            let maximumResulOverlay = self.maximumResult.intValue
            self.identifyGraphicsOverlays(atScreenPoint: screenPoint, tolerance: 15, returnPopupsOnly: false,maximumResultsPerOverlay:maximumResulOverlay) { [weak self] (result, error) in

                if let error = error {
                    reactResult["success"] = false
                    reactResult["errorMessage"] = error.localizedDescription
                } else {
                    reactResult["success"] = true
                }
                guard let result = result, !result.isEmpty else {
                    self?.onSingleTap!(reactResult)
                    return
                }
                var myReferenceId=[AnyObject]()

                if let item = result.first {
                    if item.graphicsOverlay is RNAGSGraphicsOverlay, let closestGraphic = item.graphics  as? [AGSGraphic]{

                        for value in closestGraphic{
                          if  let referenceId = value.attributes["referenceId"] as? NSString{
                          myReferenceId.append(referenceId)
                          if self?.recenterIfGraphicTapped ?? false {
                           self?.setViewpointCenter(mapPoint, completion: nil)
                          }
                          }
                        }
                    }
                }
                reactResult["graphicReferenceId"]=myReferenceId
                self?.onSingleTap!(reactResult)
            }
        }
    }


    public func geoView(_ geoView: AGSGeoView, didTouchDragToScreenPoint screenPoint: CGPoint, mapPoint: AGSPoint) {
        if let onMapMoved = onMapMoved {
            let reactResult: [AnyHashable: Any] = [
                "mapPoint" : ["latitude" : mapPoint.y, "longitude": mapPoint.x],
                "screenPoint" : ["x": screenPoint.x, "y": screenPoint.y]
            ]
        onMapMoved(reactResult)
        }

    }





    // MARK: Exposed RN Event Emitters
    @objc var onSingleTap: RCTDirectEventBlock?
    @objc var onMapDidLoad: RCTDirectEventBlock?
    @objc var onOverlayWasModified: RCTDirectEventBlock?
    @objc var onOverlayWasAdded: RCTDirectEventBlock?
    @objc var onOverlayWasRemoved: RCTDirectEventBlock?
    @objc var onMapMoved: RCTDirectEventBlock?
    @objc var onLocationChanged:RCTDirectEventBlock?
    // MARK: Exposed RN methods
    @objc func showCallout(_ args: NSDictionary) {
        let point = args["point"] as? NSDictionary
        guard let latitude = point?["latitude"] as? NSNumber, let longitude = point?["longitude"] as? NSNumber,
            let title = args["title"] as? NSString, let text = args["text"] as? NSString, let shouldRecenter = args["shouldRecenter"] as? Bool
            else {
                print("WARNING: The point object did not contian a proper latitude and longitude.")
                return
        }
        let agsPoint = AGSPoint(x: longitude.doubleValue, y: latitude.doubleValue, spatialReference: AGSSpatialReference.wgs84())
        self.callout.title = String(title)
        self.callout.detail = String(text)
        self.callout.isAccessoryButtonHidden = true
        if shouldRecenter {
            self.setViewpointCenter(agsPoint) { [weak self](_) in
                self?.callout.show(at: agsPoint, screenOffset: CGPoint.zero, rotateOffsetWithMap: false, animated: true)
            }
        } else {
            self.callout.show(at: agsPoint, screenOffset: CGPoint.zero, rotateOffsetWithMap: false, animated: true)
        }
    }

    @objc func centerMap(_ args: NSArray) {
        var points = [AGSPoint]()
        if let argsCasted = args as? [NSDictionary] {
            for rawPoint in argsCasted {
                if let latitude = rawPoint["latitude"] as? NSNumber, let longitude = rawPoint["longitude"] as? NSNumber {
                    points.append(AGSPoint(x: longitude.doubleValue, y: latitude.doubleValue, spatialReference: AGSSpatialReference.wgs84()))
                }
            }
        }
        if (points.count == 0){
            print("WARNING: Recenter point array was empty or contained invalid data.")
        } else if points.count == 1 {
            self.setViewpointCenter(points.first!, completion: nil)
        } else {
            let polygon = AGSPolygon(points: points)
            self.setViewpointGeometry(polygon, padding: 50, completion: nil)
        }
    }

    @objc func addGraphicsOverlay(_ args: NSDictionary) {
        let isRefresh = (args["refreshList"] as? Bool) ?? false
        if(isRefresh){
            self.graphicsOverlays.removeAllObjects()
            RNAGSGraphicsOverlay.stopAddGraphicQueue()
        }
        let rnRawGraphicsOverlay = RNAGSGraphicsOverlay(rawData: args)
        self.graphicsOverlays.add(rnRawGraphicsOverlay)
        if (onOverlayWasAdded != nil) {
            onOverlayWasAdded!([NSString(string: "referenceId"): rnRawGraphicsOverlay.referenceId]);
        }
    }

    @objc func addPointsToGraphicsOverlay(_ args: NSDictionary) {
        guard let name = args["overlayReferenceId"] as? NSString,
              let overlay = getOverlay(byReferenceId: name) else {
            print("WARNING: Invalid layer name entered. No points will be added.")
            reportToOverlayDidLoadListener(referenceId: args["overlayReferenceId"] as? NSString ?? "unknown", action: "add", success: false, errorMessage: "Invalid layer name entered.")
            return
        }

        guard let rawPointsCasted = args["points"] as? [NSDictionary] else {
            print("WARNING: No reference IDs provided. No points will be added.")
            reportToOverlayDidLoadListener(referenceId: name, action: "add", success: false, errorMessage: "No reference IDs provided.")
            return
        }

        var pointImageDictionary: [NSString: UIImage] = [:]
        let imageLoadGroup = DispatchGroup()
        // Process point images (either local or from URL)
        if let pointGraphics = args["pointGraphics"] as? [NSDictionary] {
            for item in pointGraphics {
                if let graphicId = item["graphicId"] as? NSString {
                    // Convert local images
                    if let graphic = RCTConvert.uiImage(item["graphic"]) {
                        pointImageDictionary[graphicId] = graphic
                    }
                    // Download image from URL
                    else if let graphicUrlString = item["graphicUrl"] as? String,
                              let graphicUrl = URL(string: graphicUrlString) {
                        imageLoadGroup.enter()// Start tracking the async task
                        
                        URLSession.shared.dataTask(with: graphicUrl) { [weak self] data, response, error in
                            defer { imageLoadGroup.leave() }// Mark task as completed
                            guard let self = self, let data = data, let image = UIImage(data: data) else {
                                print("Failed to load image from URL: \(graphicUrl) - \(error?.localizedDescription ?? "Unknown error")")
                                return
                            }
                            
                            DispatchQueue.main.async {
                                pointImageDictionary[graphicId] = image
                            }
                        }.resume()
                    }
                }
            }
        }
        // Ensure all images are loaded before adding points to the overlay
        imageLoadGroup.notify(queue: .main) {
            for item in rawPointsCasted {
                if let point = RNAGSGraphicsOverlay.createPoint(rawData: item) {
                    let graphic = RNAGSGraphicsOverlay.rnPointToAGSGraphic(point, pointImageDictionary: pointImageDictionary)
                    overlay.graphics.add(graphic)
                }
            }
            // Notify that points have been successfully added
            self.reportToOverlayDidLoadListener(referenceId: name, action: "add", success: true, errorMessage: nil)
        }
    }

    @objc func removePointsFromGraphicsOverlay(_ args: NSDictionary) {
        guard let name = args["overlayReferenceId"] as? NSString,  let overlay = getOverlay(byReferenceId: name) else {
            print("WARNING: Invalid layer name entered. No points will be removed.")
            reportToOverlayDidLoadListener(referenceId: args["overlayReferenceId"] as? NSString ?? NSString(string:"unknown"), action: "remove", success: false, errorMessage: "Invalid layer name entered.")
            return
        }
        guard let pointsToRemove = args["referenceIds"] as? [NSString] else {
            print("WARNING: No reference IDs provided. No points will be removed.")
            reportToOverlayDidLoadListener(referenceId: name, action: "remove", success: false, errorMessage: "No reference IDs provided.")
            return
        }
        for graphic in overlay.graphics as! [AGSGraphic] {
            let id = graphic.attributes["referenceId"] as! NSString
            if pointsToRemove.contains(id) {
                overlay.graphics.remove(graphic)
            }
        }
        reportToOverlayDidLoadListener(referenceId: name, action: "remove", success: true, errorMessage: nil)
    }

    @objc func updatePointsInGraphicsOverlay(_ args: NSDictionary) {
        guard let name = args["overlayReferenceId"] as? NSString,  let overlay = getOverlay(byReferenceId: name) else  {
            print("WARNING: Invalid layer name entered. No points will be modified.")
            reportToOverlayDidLoadListener(referenceId: args["overlayReferenceId"] as? NSString ?? NSString(string: "Unknown"), action: "update", success: false, errorMessage: "Invalid layer name entered.")
            return
        }
        let shouldAnimateUpdate = (args["animated"] as? Bool) ?? false
        overlay.shouldAnimateUpdate = shouldAnimateUpdate
        if let updates = args["updates"] as? [NSDictionary] {
          RNAGSGraphicsOverlay.forceResumeQueue()
            let queue = DispatchQueue(label: "queueUpdateGraphic", qos: .utility)
            queue.async {
            for update in updates {
                if(!RNAGSGraphicsOverlay.runningQueue){
                    break
                }
                overlay.updateGraphic(with: update)
            }
        }

        }
        reportToOverlayDidLoadListener(referenceId: args["overlayReferenceId"] as! NSString, action: "update", success: true, errorMessage: nil)
    }

    @objc func removeGraphicsOverlay(_ name: NSString) {
        guard let overlay = getOverlay(named: name) else {
            print("WARNING: Invalid layer name entered. No overlay will be removed.")
            return
        }
        self.graphicsOverlays.remove(overlay)
        if (onOverlayWasRemoved != nil) {
            onOverlayWasRemoved!([NSString(string: "referenceId"): name])
        }
    }

    @objc func routeGraphicsOverlay(_ args: NSDictionary) {
        guard let router = router else {
            print ("RNAGSMapView - WARNING: No router was initialized. Perhaps no routeUrl was provided?")
            return
        }
        guard let name = args["overlayReferenceId"] as? NSString,  let overlay = getOverlay(byReferenceId: name) else {
            print("RNAGSMapView - WARNING: Invalid layer name entered. No overlay will be routed.")
            return
        }
        let excludeGraphics = args["excludeGraphics"] as? [NSString]
        let color = UIColor(hex: String(args["routeColor"] as? NSString ?? "#FF0000"))!
        let module = self.bridge!.module(forName: "RNArcGISMapViewModule") as! RNArcGISMapViewModule
        module.sendIsRoutingChanged(true)
        router.createRoute(withGraphicOverlay: overlay, excludeGraphics: excludeGraphics) { [weak self] (result, error) in
            if let error = error {
                module.sendIsRoutingChanged(false)
                print("RNAGSMapView - WARNING: Error while routing: \(error.localizedDescription)")
                return
            }
            guard let result = result else {
                module.sendIsRoutingChanged(false)
                print("RNAGSMapView - WARNING: No result obtained.")
                return
            }
            // TODO: Draw routes onto graphics overlay
            print("RNAGSMapView - Route Completed")
            let generatedRoute = result.routes[0]
            self?.draw(route: generatedRoute, with: color)
            module.sendIsRoutingChanged(false)

        }
    }



    @objc func getRouteIsVisible(_ args: RCTResponseSenderBlock) {
        args([routeGraphicsOverlay.isVisible])
    }

    @objc func setRouteIsVisible(_ args: Bool){
        routeGraphicsOverlay.isVisible = args
    }

    // MARK: Exposed RN props
    @objc var basemapUrl: NSString? {
        didSet{
            // TODO: allow for basemap name to be passed depending on enum
            let basemapUrlString = String(basemapUrl ?? "")
            if (self.map == nil) {
                setUpMap()
            }
            if let url = URL(string: basemapUrlString), let basemap = AGSBasemap(url: url){
                basemap.load { [weak self] (error) in
                    if let error = error {
                        print(error.localizedDescription)
                    } else {
                        self?.map?.basemap = basemap
                    }
                }
            } else {
                print("==> Warning: Invalid Basemap URL Provided. A stock basemap will be used. <==")
            }
        }
    }

    @objc var recenterIfGraphicTapped: Bool = false

    @objc var maximumResult: NSNumber = 1


    @objc var routeUrl: NSString? {
        didSet {
            if let routeUrl = URL(string: String(routeUrl ?? "")) {
                router = RNAGSRouter(routeUrl: routeUrl)
            }
        }
    }

    @objc var initialMapCenter: NSDictionary? {
        didSet{
            handleInitCenter(polygone: initialMapCenter)
        }// end didSet
    }// end initialMapCenter declaration

    @objc var minZoom:NSNumber = 0 {
        didSet{
            self.map?.minScale = minZoom.doubleValue ?? 0
        }
    }

    @objc var maxZoom:NSNumber = 0 {
        didSet{
            self.map?.maxScale = maxZoom.doubleValue ?? 0
        }
    }

    @objc var rotationEnabled = true{
        didSet{
            self.interactionOptions.isRotateEnabled = rotationEnabled
        }
    };

    // MARK: Misc.
    private func getOverlay(byReferenceId referenceId: NSString?) -> RNAGSGraphicsOverlay? {
        if let referenceId = referenceId {
            return self.graphicsOverlays.first(where: {
                if $0 is RNAGSGraphicsOverlay {
                    return ($0 as! RNAGSGraphicsOverlay).referenceId == referenceId
                } else {
                    return false
                }
            }) as? RNAGSGraphicsOverlay
        } else {
            return nil
        }
    }

    func reportToOverlayDidLoadListener(referenceId: NSString, action: NSString, success: Bool, errorMessage: NSString?){
        if (onOverlayWasModified != nil) {
            var reactResult: [AnyHashable: Any] = [
                "referenceId" : referenceId, "action": action, "success": success
            ]
            if let errorMessage = errorMessage {
                reactResult["errorMessage"] = errorMessage
            }
            onOverlayWasModified!(reactResult)
        }
    }
    private func getOverlay(named name: NSString) -> RNAGSGraphicsOverlay?{
        return self.graphicsOverlays.first(where: { (item) -> Bool in
            guard let item = item as? RNAGSGraphicsOverlay else {
                return false
            }
            return item.referenceId == name
        }) as? RNAGSGraphicsOverlay
    }

    private func draw(route: AGSRoute, with color: UIColor){
        DispatchQueue.main.async {
            self.routeGraphicsOverlay.graphics.removeAllObjects()
            let routeSymbol = AGSSimpleLineSymbol(style: .solid, color: color, width: 5)
            let routeGraphic = AGSGraphic(geometry: route.routeGeometry, symbol: routeSymbol, attributes: nil)
            self.routeGraphicsOverlay.graphics.add(routeGraphic)
        }
    }
    func handleInitCenter(polygone initialMapCenter: NSDictionary?) {
        initialCenterPoint = initialMapCenter
        var points = [AGSPoint]()
        if let initialMapCenter = initialMapCenter?["points"] as? [NSDictionary] {
            for rawPoint in initialMapCenter {
                if let latitude = rawPoint["latitude"] as? NSNumber, let longitude = rawPoint["longitude"] as? NSNumber {
                    points.append(AGSPoint(x: longitude.doubleValue, y: latitude.doubleValue, spatialReference: AGSSpatialReference.wgs84()))
                } // end if let
            }// end for loop
        } // end initialmapcenter nil check
        // If no points exist, add a sample point
        if points.count == 0 {
            points.append(AGSPoint(x: 36.244797, y: -94.148060, spatialReference: AGSSpatialReference.wgs84()))
        }
        if points.count == 1 {
            let viewpoint = AGSViewpoint(center: points.first!, scale: 10000)
            self.map?.initialViewpoint = viewpoint
        } else {

    let graphicsOverlay = AGSGraphicsOverlay()
            var colorStroke = hexStringToUIColor(hex: "#B71D21")
    self.graphicsOverlays.add(graphicsOverlay)

    let polygon = AGSPolygon(points: points)
    let stroke=initialMapCenter?["stroke"] as? CGFloat
    let polygonSymbol = AGSSimpleFillSymbol(style: AGSSimpleFillSymbolStyle.null, color: .orange, outline: AGSSimpleLineSymbol(style: .solid, color: colorStroke, width: stroke ?? 1.0))
    let polygonGraphic = AGSGraphic(geometry: polygon, symbol: polygonSymbol)
    graphicsOverlay.graphics.add(polygonGraphic)
    //set mapcenter and scale
    let scale=initialMapCenter?["mapScale"] as? Double
    let targetScale=scale ?? 0.5
    let paddingWidth=polygon.extent.width * targetScale
    let paddingHeight=polygon.extent.height * targetScale
    let envelope = AGSEnvelope(xMin: polygon.extent.xMin-paddingWidth, yMin:polygon.extent.yMin-paddingHeight, xMax:polygon.extent.xMax+paddingWidth, yMax:polygon.extent.yMax+paddingHeight, spatialReference: .wgs84())

            self.setViewpoint(AGSViewpoint(targetExtent: envelope))
        }

    }
    public func reloadMap() {
        let maxZoom=self.map?.maxScale
        let minZoom=self.map?.minScale
        let centerCurrent =  self.center
        setUpMap()
        handleInitCenter(polygone: initialCenterPoint)
        self.map?.minScale = minZoom ?? 0
        self.map?.maxScale = maxZoom ?? 0
        }

// Assumes input like "#00FF00" (#RRGGBB).
func hexStringToUIColor (hex:String) -> UIColor {
    var cString:String = hex.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()

    if (cString.hasPrefix("#")) {
        cString.remove(at: cString.startIndex)
    }

    if ((cString.count) != 6) {
        return UIColor.gray
    }

    var rgbValue:UInt32 = 0
    Scanner(string: cString).scanHexInt32(&rgbValue)

    return UIColor(
        red: CGFloat((rgbValue & 0xFF0000) >> 16) / 255.0,
        green: CGFloat((rgbValue & 0x00FF00) >> 8) / 255.0,
        blue: CGFloat(rgbValue & 0x0000FF) / 255.0,
        alpha: CGFloat(1.0)
    )
}

}
