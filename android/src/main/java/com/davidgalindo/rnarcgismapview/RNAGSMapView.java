package com.davidgalindo.rnarcgismapview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.esri.arcgisruntime.ArcGISRuntimeException;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeodeticCurveType;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.LinearUnit;
import com.esri.arcgisruntime.geometry.LinearUnitId;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.PolygonBuilder;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.SketchCreationMode;
import com.esri.arcgisruntime.mapping.view.SketchEditor;
import com.esri.arcgisruntime.mapping.view.SketchGeometryChangedEvent;
import com.esri.arcgisruntime.mapping.view.SketchGeometryChangedListener;
import com.esri.arcgisruntime.mapping.view.SketchStyle;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.tasks.networkanalysis.Route;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult;
import com.esri.arcgisruntime.util.ListenableList;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class RNAGSMapView extends LinearLayout implements LifecycleEventListener {
    // MARK: Variables/Prop declarations
    private final String TAG = RNAGSMapView.class.getSimpleName();
    View rootView;
    public MapView mapView;
    String basemapUrl = "";
    String routeUrl = "";
    Boolean recenterIfGraphicTapped = false;
    HashMap<String, RNAGSGraphicsOverlay> rnGraphicsOverlays = new HashMap<>();
    GraphicsOverlay routeGraphicsOverlay;
    RNAGSRouter router;
    private Callout callout;
    private SimpleMarkerSymbol mPointSymbol;
    private SimpleLineSymbol mLineSymbol;
    private SimpleFillSymbol mFillSymbol;
    Double minZoom = 0.0;
    Double maxZoom = 0.0;
    Graphic mGraphic,lineGraphic ;
    Boolean rotationEnabled = true;

    private SketchEditor mSketchEditor;

    private SketchGeometryChangedListener sketchGeometryChangedListener;
    // MARK: Initializers
    public RNAGSMapView(Context context) {
        super(context);
        rootView = inflate(context.getApplicationContext(), R.layout.rnags_mapview, this);
        mapView = rootView.findViewById(R.id.agsMapView);
        if (context instanceof ReactContext) {
            ((ReactContext) context).addLifecycleEventListener(this);
        }

        ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC_VECTOR, 34.056295, -117.195800, 16);
        mapView.setMap(map);
        routeGraphicsOverlay = new GraphicsOverlay();
        mapView.getGraphicsOverlays().add(routeGraphicsOverlay);
        mapView.setOnTouchListener(new OnSingleTouchListener(getContext(), mapView));

        // define symbols
        mPointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.parseColor("#2B64F5"), 20);
        mLineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID,  Color.parseColor("#2B64F5"), 2);
        mFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.parseColor("#5E2B64F5"),mLineSymbol);

        // create a new sketch editor and add it to the map view
        mSketchEditor = new SketchEditor();
        mapView.setSketchEditor(mSketchEditor);

        SketchStyle style = mSketchEditor.getSketchStyle();
        style.getLineSymbol().setColor(Color.parseColor("#2B64F5"));
        style.getLineSymbol().setWidth(3.0f);
        style.getFillSymbol().setColor(Color.parseColor("#5E2B64F5"));
//        style.setFillSymbol(mFillSymbol);
        style.setVertexSymbol(new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.parseColor("#5E2B64F5"), 20));
        style.setSelectedVertexSymbol(new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.parseColor("#2B64F5"), 10));
        mSketchEditor.setSketchStyle(style);

        sketchGeometryChangedListener = new SketchGeometryChangedListener() {
            @Override
            public void geometryChanged(SketchGeometryChangedEvent sketchGeometryChangedEvent) {
                if (sketchGeometryChangedEvent.getGeometry() != null) {
                    Log.d(TAG, "abhishek: " + sketchGeometryChangedEvent.getGeometry().getGeometryType());
                    try {
                        switch (sketchGeometryChangedEvent.getGeometry().getGeometryType()) {
                            case POLYLINE:
                                final Polyline wgs84Point1 = (Polyline) GeometryEngine.project(sketchGeometryChangedEvent.getGeometry(), SpatialReferences.getWgs84());
                                String jsonStr1 = wgs84Point1.toJson();
                                JSONObject obj1 = new JSONObject(jsonStr1);
                                Log.d(TAG, "geometryChanged: "+obj1);
                                JSONArray jsonArr1 = new JSONArray(obj1.getString("paths"));
                                WritableMap map1 = Arguments.createMap();
                                map1.putBoolean("success", false);
                                map1.putString("response", jsonArr1.toString());
                                Log.d(TAG, "POLYLINE: " + map1);
                                emitEvent("onDrawPoligon", map1);
                                break;
                            case POLYGON:
                                final Polygon wgs84Point = (Polygon) GeometryEngine.project(sketchGeometryChangedEvent.getGeometry(), SpatialReferences.getWgs84());
                                String jsonStr = wgs84Point.toJson();
//                                Log.d(TAG, "geometryChanged: " + jsonStr);
                                JSONObject obj = new JSONObject(jsonStr);
                                JSONArray jsonArr = new JSONArray(obj.getString("rings"));
                                WritableMap map = Arguments.createMap();
                                map.putBoolean("success", false);
                                map.putString("response", jsonArr.toString());
                                emitEvent("onDrawPoligon", map);
                                break;
                            default:
                                Log.d(TAG, "default: ");
                                break;
                        }

                    } catch (JSONException e) {
                        Log.d(TAG, "JSONException: "+e.getMessage());
                    }
                }
            }
        };
        mSketchEditor.addGeometryChangedListener(sketchGeometryChangedListener);
//        setUpMap();
//        setUpCallout();
    }

    private void setUpCallout() {
        // We want to create the callout right after the View has finished its layout
        mapView.post(() -> {
            callout = mapView.getCallout();
            callout.setContent(inflate(getContext().getApplicationContext(), R.layout.rnags_callout_content, null));
            Callout.ShowOptions showOptions = new Callout.ShowOptions();
            showOptions.setAnimateCallout(true);
            showOptions.setAnimateRecenter(true);
            showOptions.setRecenterMap(false);
            callout.getStyle().setMaxHeight(1000);
            callout.getStyle().setMaxWidth(1000);
            callout.getStyle().setMinHeight(100);
            callout.getStyle().setMinWidth(100);
            callout.setShowOptions(showOptions);
            callout.setPassTouchEventsToMapView(false);
        });

    }

    @SuppressLint("ClickableViewAccessibility")
    public void setUpMap() {

        mapView.getMap().addDoneLoadingListener(() -> {
            ArcGISRuntimeException e = mapView.getMap().getLoadError();
            Boolean success = e != null;
            String errorMessage = !success ? "" : e.getMessage();
            WritableMap map = Arguments.createMap();
            map.putBoolean("success", success);
            map.putString("errorMessage", errorMessage);
            emitEvent("onMapDidLoad", map);
        });
    }


    public void setRouteUrl(String url) {
        routeUrl = url;
        router = new RNAGSRouter(getContext().getApplicationContext(), routeUrl);
    }

    public void setRecenterIfGraphicTapped(boolean value) {
        recenterIfGraphicTapped = value;
    }

    public void setInitialMapCenter(ReadableArray initialCenter) {
        ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i < initialCenter.size(); i++) {
            ReadableMap item = initialCenter.getMap(i);
            if (item == null) {
                continue;
            }
            Double latitude = item.getDouble("latitude");
            Double longitude = item.getDouble("longitude");
            if (latitude == 0 || longitude == 0) {
                continue;
            }
            Point point = new Point(longitude, latitude, SpatialReferences.getWgs84());
            points.add(point);
        }
        // If no points exist, add a sample point
        if (points.size() == 0) {
            points.add(new Point(36.244797, -94.148060, SpatialReferences.getWgs84()));
        }
        if (points.size() == 1) {
            mapView.getMap().setInitialViewpoint(new Viewpoint(points.get(0), 10000));
        } else {
            Polygon polygon = new Polygon(new PointCollection(points));
            Viewpoint viewpoint = viewpointFromPolygon(polygon);
            mapView.getMap().setInitialViewpoint(viewpoint);
        }
    }

    public void setMinZoom(Double value) {
        minZoom = value;
        mapView.getMap().setMinScale(minZoom);
    }

    public void setMaxZoom(Double value) {
        maxZoom = value;
        mapView.getMap().setMaxScale(maxZoom);
    }

    public void setRotationEnabled(Boolean value) {
        rotationEnabled = value;
    }

    // MARK: Exposed RN Methods
    public void showCallout(ReadableMap args) {
        ReadableMap rawPoint = args.getMap("point");
        if (!rawPoint.hasKey("latitude") || !rawPoint.hasKey("longitude")) {
            return;
        }
        Double latitude = rawPoint.getDouble("latitude");
        Double longitude = rawPoint.getDouble("longitude");
        if (latitude == 0 || longitude == 0) {
            return;
        }
        String title = "";
        String text = "";
        Boolean shouldRecenter = false;

        if (args.hasKey("title")) {
            title = args.getString("title");
        }
        if (args.hasKey("text")) {
            text = args.getString("text");
        }
        if (args.hasKey("shouldRecenter")) {
            shouldRecenter = args.getBoolean("shouldRecenter");
        }

        // Displaying the callout
        Point agsPoint = new Point(longitude, latitude, SpatialReferences.getWgs84());

        // Set callout content
        View calloutContent = callout.getContent();
        ((TextView) calloutContent.findViewById(R.id.title)).setText(title);
        ((TextView) calloutContent.findViewById(R.id.text)).setText(text);
        callout.setLocation(agsPoint);
        callout.show();
        Log.i("AGS", callout.isShowing() + " " + calloutContent.getWidth() + " " + calloutContent.getHeight());
        if (shouldRecenter) {
            mapView.setViewpointCenterAsync(agsPoint);
        }
    }

    // MARK: Prop set methods
    public void setBasemapUrl(String url) {
        basemapUrl = url;
        if (basemapUrl == null || basemapUrl.isEmpty()) {
            return;
        }
        // Set basemap of map
        if (mapView.getMap() == null) {
            setUpMap();
        }
        final Basemap basemap = new Basemap(basemapUrl);
        basemap.addDoneLoadingListener(() -> {
            if (basemap.getLoadError() != null) {
                Log.w("AGSMap", "An error occurred: " + basemap.getLoadError().getMessage());
            } else {
                mapView.getMap().setBasemap(basemap);
            }
        });
        basemap.loadAsync();
    }
    public void centerMap(ReadableArray args) {
        final ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i < args.size(); i++) {
            ReadableMap item = args.getMap(i);
            if (item == null) {
                continue;
            }
            Double latitude = item.getDouble("latitude");
            Double longitude = item.getDouble("longitude");
            if (latitude == 0 || longitude == 0) {
                continue;
            }
            points.add(new Point(longitude, latitude, SpatialReferences.getWgs84()));
        }
        // Perform the recentering
        if (points.size() == 1) {
            mapView.setViewpointCenterAsync(points.get(0), 60000);
        } else if (points.size() > 1) {
            PointCollection pointCollection = new PointCollection(points);
            Polygon polygon = new Polygon(pointCollection);
            mapView.setViewpointGeometryAsync(polygon, 50);
        }

    }

    private void setupSymbols(ReadableMap args,Double latitude, Double longitude) {

        GraphicsOverlay graphicsOverlay = new GraphicsOverlay();

        //add the overlay to the map view
        mapView.getGraphicsOverlays().add(graphicsOverlay);

        //[DocRef: Name=Picture Marker Symbol Drawable-android, Category=Fundamentals, Topic=Symbols and Renderers]
        //Create a picture marker symbol from an app resource
        BitmapDrawable selectedMapPin = (BitmapDrawable) ContextCompat.getDrawable(getContext(), R.drawable.mappin);
        BitmapDrawable unSelectedMapPin = (BitmapDrawable) ContextCompat.getDrawable(getContext(), R.drawable.backarrow);
        final PictureMarkerSymbol selectedPinSourceSymbol,unSelectedPinSourceSymbol;
        try {
            selectedPinSourceSymbol = PictureMarkerSymbol.createAsync(selectedMapPin).get();
            selectedPinSourceSymbol.loadAsync();
            selectedPinSourceSymbol.setOffsetY(20);
            selectedPinSourceSymbol.setWidth(20);
            selectedPinSourceSymbol.setHeight(20);

            unSelectedPinSourceSymbol = PictureMarkerSymbol.createAsync(unSelectedMapPin).get();
            unSelectedPinSourceSymbol.loadAsync();
            unSelectedPinSourceSymbol.setOffsetY(20);
            unSelectedPinSourceSymbol.setWidth(20);
            unSelectedPinSourceSymbol.setHeight(20);


            selectedPinSourceSymbol.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: ");
                    ReadableArray arr = args.getArray("points");
                    for (int i = 0; i < arr.size(); i++) {
                        if (longitude == arr.getMap(i).getDouble("longitude") && latitude == arr.getMap(i).getDouble("latitude")) {
                            Log.d(TAG, "run: matched");
                            Point mSourcePoint = new Point(arr.getMap(i).getDouble("longitude"), arr.getMap(i).getDouble("latitude"), SpatialReferences.getWgs84());
                            Graphic pinSourceGraphic = new Graphic(mSourcePoint, selectedPinSourceSymbol);
                            graphicsOverlay.getGraphics().remove(pinSourceGraphic);
                        }else{
                            Point mSourcePoint = new Point(arr.getMap(i).getDouble("longitude"), arr.getMap(i).getDouble("latitude"), SpatialReferences.getWgs84());
                            Graphic pinSourceGraphic = new Graphic(mSourcePoint, selectedPinSourceSymbol);
                            graphicsOverlay.getGraphics().add(pinSourceGraphic);
                        }

                    }
                }
            });

            unSelectedPinSourceSymbol.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    ReadableArray arr = args.getArray("points");
                    for (int i = 0; i < arr.size(); i++) {
                        if (longitude == arr.getMap(i).getDouble("longitude") && latitude == arr.getMap(i).getDouble("latitude")) {
                            Log.d(TAG, "run: matched");
                            Point mSourcePoint = new Point(arr.getMap(i).getDouble("longitude"), arr.getMap(i).getDouble("latitude"), SpatialReferences.getWgs84());
                            Graphic pinSourceGraphic = new Graphic(mSourcePoint, unSelectedPinSourceSymbol);
                            graphicsOverlay.getGraphics().add(pinSourceGraphic);
                        }
                    }
                }
            });

            ReadableArray arr =  args.getArray("points");
            PointCollection collection = new PointCollection(SpatialReferences.getWgs84());
            for (int i = 0; i< arr.size();i++){
                collection.add(new Point(arr.getMap(i).getDouble("latitude"), arr.getMap(i).getDouble("longitude")));
            }

            Polygon myPolygon = new Polygon(collection);
            mapView.setViewpointGeometryAsync(myPolygon,75);

        } catch (InterruptedException e) {
            Log.d(TAG, "setupSymbols: error 1"+e.getMessage());
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.d(TAG, "setupSymbols: error 2"+e.getMessage());
            e.printStackTrace();
        }
    }

    // Layer add/remove

    public void addGraphicsOverlay(ReadableMap args) {
        this.setupSymbols(args,0.0,0.0);
    }

//    public void removeGraphicsOverlay(String removalId) {
//        if(removalId.length() > 0) {
//            RNAGSGraphicsOverlay overlay = rnGraphicsOverlays.get(removalId);
//            if (overlay == null) {
//                Log.w("Warning (AGS)", "No overlay with the associated ID was found.");
//                return;
//            }
//            mapView.getGraphicsOverlays().remove(overlay.getAGSGraphicsOverlay());
//            rnGraphicsOverlays.remove(removalId);
//            routeGraphicsOverlay.getGraphics().remove(mGraphic);
//            routeGraphicsOverlay.getGraphics().remove(lineGraphic);
//        } else {
//            routeGraphicsOverlay.getGraphics().remove(mGraphic);
//            routeGraphicsOverlay.getGraphics().remove(lineGraphic);
//        }
//    }
//
//


    public void removeGraphicsOverlay(String removalId) {
        if(removalId.length() > 0) {
            RNAGSGraphicsOverlay overlay = rnGraphicsOverlays.get(removalId);
            if (overlay == null) {
                Log.w("Warning (AGS)", "No overlay with the associated ID was found.");
                return;
            }
            mapView.getGraphicsOverlays().remove(overlay.getAGSGraphicsOverlay());
            rnGraphicsOverlays.remove(removalId);
            // routeGraphicsOverlay.getGraphics().remove(mGraphic);
            // routeGraphicsOverlay.getGraphics().remove(lineGraphic);
        } else {
            routeGraphicsOverlay.getGraphics().remove(mGraphic);
            routeGraphicsOverlay.getGraphics().remove(lineGraphic);
        }
    }

    // Point updates
    public void updatePointsInGraphicsOverlay(ReadableMap args) {
//        if (!args.hasKey("overlayReferenceId")) {
//            Log.w("Warning (AGS)", "No overlay with the associated ID was found.");
//            return;
//        }
//        Boolean shouldAnimateUpdate = false;
//        if (args.hasKey("animated")) {
//            shouldAnimateUpdate = args.getBoolean("animated");
//        }
//        String overlayReferenceId = args.getString("overlayReferenceId");
//        RNAGSGraphicsOverlay overlay = rnGraphicsOverlays.get(overlayReferenceId);
//        if (overlay != null && args.hasKey("updates")) {
//            overlay.setShouldAnimateUpdate(shouldAnimateUpdate);
//            overlay.updateGraphics(args.getArray("updates"));
//        }
        Log.d(TAG, "updatePointsInGraphicsOverlay: "+args);
        this.setupSymbols(args,args.getDouble("longitude"),args.getDouble("latitude"));

    }

    public void addPointsToOverlay(ReadableMap args) {

        PolygonBuilder polygonGeometry = new PolygonBuilder(SpatialReferences.getWebMercator());
        Log.d(TAG, "addPointsToOverlay: "+polygonGeometry);
                if (!args.hasKey("points")) {
            Log.w("Warning (AGS)", "No overlay with the associated ID was found.");
            return;
        }else{
                   ReadableArray arr =  args.getArray("points");
                   PointCollection collection = new PointCollection(SpatialReferences.getWgs84());
                  for (int i = 0; i< arr.size();i++){
                      Log.d(TAG, "addPointsToOverlay: "+arr.getMap(i).getDouble("latitude")+ arr.getMap(i).getDouble("longitude"));
                      collection.add(new Point(arr.getMap(i).getDouble("latitude"), arr.getMap(i).getDouble("longitude")));
//                      polygonGeometry.addPoint(arr.getMap(i).getDouble("latitude"), arr.getMap(i).getDouble("longitude"));
                  }

                  Polygon polyline = new Polygon(collection);

                    // create a polyline graphic with the polyline geometry and symbol
                    Graphic polylineGraphic = new Graphic(polyline, mFillSymbol);

                    GraphicsOverlay grOverlay = new GraphicsOverlay();
                    // create list of graphics
                    ListenableList<Graphic> graphics = grOverlay.getGraphics();
                    // add graphic to graphics overlay
                    graphics.add(polylineGraphic);

                    // add graphics overlay to the MapView
                    mapView.getGraphicsOverlays().add(grOverlay);



                    // add the polyline graphic to the graphics overlay
//                    graphicsOverlay.graphics.add(polylineGraphic)
//                    mapView.getGraphicsOverlays().add(polylineGraphic);

                }



//
//        if (!args.hasKey("overlayReferenceId")) {
//            Log.w("Warning (AGS)", "No overlay with the associated ID was found.");
//            return;
//        }
//        String overlayReferenceId = args.getString("overlayReferenceId");
//        RNAGSGraphicsOverlay overlay = rnGraphicsOverlays.get(overlayReferenceId);
//        if (overlay != null && args.hasKey("points")) {
//            overlay.addGraphics(args.getArray("points"));
//        }
    }

    public String addSketchToMap(int sketchType) {
        routeGraphicsOverlay.getGraphics().remove(mGraphic);
        routeGraphicsOverlay.getGraphics().remove(lineGraphic);
        switch (sketchType) {
            case 1 :
                mSketchEditor.stop(); // will this fix a bug with changing draw modes w/o stopping
                mSketchEditor.start(SketchCreationMode.FREEHAND_POLYGON);
                break;
            case 2 :
                mSketchEditor.stop(); // will this fix a bug with changing draw modes w/o stopping
                mSketchEditor.start(SketchCreationMode.RECTANGLE);
                break;
            case 3 :
                mSketchEditor.stop(); // will this fix a bug with changing draw modes w/o stopping
                mSketchEditor.start(SketchCreationMode.FREEHAND_LINE);
                break;
            case 4 :
                mSketchEditor.stop();
            default:
                break;
        }
        return "Done";
    }

    public String stopSketchOnMap() {
        Log.d(TAG, "stopSketchOnMap: ");
        mSketchEditor.stop();
//        mSketchEditor.removeGeometryChangedListener(sketchGeometryChangedListener);
//        mapView.setSketchEditor(mSketchEditor);
//        stop();
        return "Stoped";
    }

    public String stopSketchDrawLayer () {
        stop();
        return "Stop Sketch";
    }

    /**
     * When the stop button is clicked, check that sketch is valid. If so, get the geometry from the sketch, set its
     * symbol and add it to the graphics overlay.
     */
    private void stop() {
        Log.d(TAG, "stop: ");
        if (!mSketchEditor.isSketchValid()) {
            mSketchEditor.stop();
            return;
        }

        // get the geometry from sketch editor
        Geometry sketchGeometry = mSketchEditor.getGeometry();

        mSketchEditor.stop();
        routeGraphicsOverlay.getGraphics().remove(mGraphic);
        routeGraphicsOverlay.getGraphics().remove(lineGraphic);
//        if (lineGraphic != null) {
//            Log.d(TAG, "stop: ");
//            routeGraphicsOverlay.getGraphics().clear();
//        }

        if (sketchGeometry != null) {

            // create a graphic from the sketch editor geometry
            mGraphic = new Graphic(sketchGeometry);

            // assign a symbol based on geometry type
            if (mGraphic.getGeometry().getGeometryType() == GeometryType.POLYGON ) {
                mGraphic.setSymbol(mFillSymbol);
            } else if (mGraphic.getGeometry().getGeometryType() == GeometryType.POLYLINE) {
                Log.d(TAG, "stop: ");
//                Polygon mPolygon = GeometryEngine.buffer(mGraphic.getGeometry(),20);
                Polygon mPolygon = GeometryEngine.bufferGeodetic(mGraphic.getGeometry(),20, new LinearUnit(LinearUnitId.METERS),1.0,GeodeticCurveType.SHAPE_PRESERVING);
                lineGraphic = new Graphic(mPolygon, mFillSymbol);
                routeGraphicsOverlay.getGraphics().add(lineGraphic);
            } else if (mGraphic.getGeometry().getGeometryType() == GeometryType.POINT ||
                    mGraphic.getGeometry().getGeometryType() == GeometryType.MULTIPOINT) {
                mGraphic.setSymbol(mPointSymbol);
            }
            else
                mGraphic.setSymbol(mFillSymbol);
            // add the graphic to the graphics overlay
            routeGraphicsOverlay.getGraphics().add(mGraphic);
//            routeGraphicsOverlay.getGraphics().remove(graphic);
        }
    }

    public void removePointsFromOverlay(ReadableMap args) {
        if (!args.hasKey("overlayReferenceId")) {
            Log.w("Warning (AGS)", "No overlay with the associated ID was found.");
            return;
        }
        String overlayReferenceId = args.getString("overlayReferenceId");
        RNAGSGraphicsOverlay overlay = rnGraphicsOverlays.get(overlayReferenceId);
        if (overlay != null && args.hasKey("referenceIds")) {
            overlay.removeGraphics(args.getArray("referenceIds"));
        }
    }

    // set/get
    public void setRouteIsVisible(Boolean isVisible) {
        routeGraphicsOverlay.setVisible(isVisible);
    }

    public Boolean getRouteIsVisible() {
        return routeGraphicsOverlay.isVisible();
    }

    // Routing
    public void routeGraphicsOverlay(ReadableMap args) {
        if (router == null) {
            Log.w("Warning (AGS)", "Router has not been initialized. Perhaps no route URL was provided? Route URL:" + routeUrl);
            return;
        }
        if (!args.hasKey("overlayReferenceId")) {
            Log.w("Warning (AGS)", "No overlay with the associated ID was found.");
            return;
        }
        String overlayReferenceId = args.getString("overlayReferenceId");
        RNAGSGraphicsOverlay overlay = rnGraphicsOverlays.get(overlayReferenceId);
        ArrayList<String> removeGraphics = new ArrayList<>();
        if (args.hasKey("excludeGraphics")) {
            ReadableArray rawArray = args.getArray("excludeGraphics");
            for (Object item : rawArray.toArrayList()) {
                removeGraphics.add(((String) item));
            }
        }
        final String color;
        if (args.hasKey("routeColor")) {
            color = args.getString("routeColor");
        } else {
            color = "#FF0000";
        }
        assert overlay != null;
        ListenableFuture<RouteResult> future = router.createRoute(overlay.getAGSGraphicsOverlay(), removeGraphics);
        if (future == null) {
            Log.w("Warning (AGS)", "There was an issue creating the route. Please try again later, or check your routing server.");
            return;
        }
        setIsRouting(true);
        future.addDoneListener(() -> {
            try {
                RouteResult result = future.get();
                Log.d(TAG, "routeGraphicsOverlay: ");
                if (result != null && !result.getRoutes().isEmpty()) {
                    Route route = result.getRoutes().get(0);
                    drawRoute(route, color);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                setIsRouting(false);
            }
        });
    }

    private void setIsRouting(Boolean value) {
        ((ReactContext) getContext()).getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("isRoutingChanged", value);
    }

    private void drawRoute(Route route, String color) {
        if (route == null) {
            Log.w("Warning (AGS)", "No route result returned.");
            return;
        }
        routeGraphicsOverlay.getGraphics().clear();
        SimpleLineSymbol symbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.parseColor(color), 5);
        Polyline polyline = route.getRouteGeometry();
        Graphic routeGraphic = new Graphic(route.getRouteGeometry(), symbol);
        routeGraphicsOverlay.getGraphics().add(routeGraphic);
    }

    // MARK: Event emitting
    public void emitEvent(String eventName, WritableMap args) {
//        Log.d(TAG, "emitEvent: "+args+eventName);
        ((ReactContext) getContext()).getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                eventName,
                args
        );
    }


    // MARK: OnTouchListener
    public class OnSingleTouchListener extends DefaultMapViewOnTouchListener {
        OnSingleTouchListener(Context context, MapView mMapView) {
            super(context, mMapView);
            Log.d(TAG, "OnSingleTouchListener: ");
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: ");
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            Log.d(TAG, "onDoubleTapEvent: ");
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onDoubleTouchDrag(MotionEvent event) {
            Log.d(TAG, "onDoubleTouchDrag: "+event);
            return super.onDoubleTouchDrag(event);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG, "onFling: ");
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.d(TAG, "onScroll: ");
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            Log.d(TAG, "onScaleEnd: ");
            super.onScaleEnd(detector);
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            Log.d(TAG, "onTouch: ");
            return super.onTouch(view, event);
        }

        @Override
        public boolean onRotate(MotionEvent event, double rotationAngle) {
            if (rotationEnabled) {
                return super.onRotate(event, rotationAngle);
            } else {
                return true;
            }
        }

        @Override
        public boolean onDown(MotionEvent e) {
            WritableMap map = createPointMap(e);
            emitEvent("onMapMoved", map);
            return true;
        }

        private WritableMap createPointMap(MotionEvent e) {
            android.graphics.Point screenPoint = new android.graphics.Point(((int) e.getX()), ((int) e.getY()));
            WritableMap screenPointMap = Arguments.createMap();
            screenPointMap.putInt("x", screenPoint.x);
            screenPointMap.putInt("y", screenPoint.y);
            Point mapPoint = mMapView.screenToLocation(screenPoint);
            WritableMap mapPointMap = Arguments.createMap();
            if (mapPoint != null) {
                Point latLongPoint = ((Point) GeometryEngine.project(mapPoint, SpatialReferences.getWgs84()));

                mapPointMap.putDouble("latitude", latLongPoint.getY());
                mapPointMap.putDouble("longitude", latLongPoint.getX());
            }
            WritableMap map = Arguments.createMap();
            map.putMap("screenPoint", screenPointMap);
            map.putMap("mapPoint", mapPointMap);
            return map;
        }



        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            WritableMap map = createPointMap(e);
            android.graphics.Point screenPoint = new android.graphics.Point(((int) e.getX()), ((int) e.getY()));
            ListenableFuture<List<IdentifyGraphicsOverlayResult>> future = mMapView.identifyGraphicsOverlaysAsync(screenPoint, 15, false);
            future.addDoneListener(() -> {
                try {
                    if (!future.get().isEmpty()) {
                        // We only care about the topmost result
                        IdentifyGraphicsOverlayResult futureResult = future.get().get(0);
                        List<Graphic> graphicResult = futureResult.getGraphics();
                        // More null checking >.>
                        if (!graphicResult.isEmpty()) {
                            Graphic result = graphicResult.get(0);
//                            Point latLongPoint = ((Point) GeometryEngine.project(mapPoint, SpatialReferences.getWgs84()));
                            Log.d(TAG, "onSingleTapConfirmed: "+result.getGeometry().getGeometryType());
                            if (result.getGeometry() != null) {
                                Log.d(TAG, "abhishek: " + result.getGeometry().getGeometryType());
                                try {
                                    switch (result.getGeometry().getGeometryType()) {
                                        case POLYLINE:
                                            final Polyline wgs84Point1 = (Polyline) GeometryEngine.project(result.getGeometry(), SpatialReferences.getWgs84());
                                            String jsonStr1 = wgs84Point1.toJson();
                                            JSONObject obj1 = new JSONObject(jsonStr1);
                                            Log.d(TAG, "geometryChanged: "+obj1);
                                            JSONArray jsonArr1 = new JSONArray(obj1.getString("paths"));
                                            WritableMap map1 = Arguments.createMap();
                                            map1.putBoolean("success", true);
                                            map1.putString("response", jsonArr1.toString());
                                            Log.d(TAG, "POLYLINE: " + map1);
//                                            emitEvent("onDrawPoligon", map1);
                                            break;
                                        case POLYGON:
                                            final Polygon wgs84Point = (Polygon) GeometryEngine.project(result.getGeometry(), SpatialReferences.getWgs84());
                                            String jsonStr = wgs84Point.toJson();
                                            JSONObject obj = new JSONObject(jsonStr);
                                            JSONArray jsonArr = new JSONArray(obj.getString("rings"));
                                            WritableMap mapp = Arguments.createMap();
                                            mapp.putBoolean("success", true);
                                            mapp.putString("response", jsonArr.toString());
//                                            emitEvent("onDrawPoligon", mapp);
                                            break;
                                        case POINT:
                                            Log.d(TAG, "POINT: "+result.getGeometry().toJson());
                                            WritableMap newWriteMap = Arguments.createMap();
                                            newWriteMap.putBoolean("success", true);
                                            final Point myPoint = (Point) GeometryEngine.project(result.getGeometry(), SpatialReferences.getWgs84());
                                            newWriteMap.putDouble("latitude",myPoint.getX());
                                            newWriteMap.putDouble("longitude",myPoint.getY());
                                            newWriteMap.putString("response","");
                                            emitEvent("onSingleTap", newWriteMap);
                                            break;
                                        default:
                                            Log.d(TAG, "default: "+result.getGeometry().toJson());
                                            break;
                                    }

                                } catch (JSONException error) {
                                    Log.d(TAG, "JSONException: "+error.getMessage());
                                }
                            }

//                            Log.d(TAG, "onSingleTapConfirmed: "+result.getAttributes().get("referenceId"));
//                            if (result != null) {
//                                Map<String,Object> mMap = result.getAttributes();
//                                if (mMap != null && mMap.get("referenceId") != null){
//                                    map.putString("graphicReferenceId", Objects.requireNonNull(result.getAttributes().get("referenceId")).toString());
//                                }
//                            }
//                            if (recenterIfGraphicTapped) {
//                                mapView.setViewpointCenterAsync(((Point) result.getGeometry()));
//                            }
                        }
                    }
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                } finally {
//                    emitEvent("onSingleTap", map);
                }
            });
            return true;
        }
    }

    // MARK: Lifecycle Event Listeners
    @Override
    public void onHostResume() {
        mapView.resume();
    }

    @Override
    public void onHostPause() {
        mapView.pause();
    }

    @Override
    public void onHostDestroy() {
        mapView.dispose();
        if (getContext() instanceof ReactContext) {
            ((ReactContext) getContext()).removeLifecycleEventListener(this);
        }
    }

    // MARK: Misc.
    public Viewpoint viewpointFromPolygon(Polygon polygon) {
        Envelope envelope = polygon.getExtent();
        Double paddingWidth = envelope.getWidth() * 0.5;
        Double paddingHeight = envelope.getHeight() * 0.5;
        return new Viewpoint(new Envelope(
                envelope.getXMin() - paddingWidth, envelope.getYMax() + paddingHeight,
                envelope.getXMax() + paddingWidth, envelope.getYMin() - paddingHeight,
                SpatialReferences.getWgs84()), 0);
    }
}
