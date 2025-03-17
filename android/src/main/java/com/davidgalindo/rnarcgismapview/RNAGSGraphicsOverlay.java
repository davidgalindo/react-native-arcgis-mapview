package com.davidgalindo.rnarcgismapview;

import android.graphics.Color;
import android.util.Log;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class RNAGSGraphicsOverlay {
    private GraphicsOverlay graphicsOverlay;
    private HashMap<String, String> pointImageDictionary;
    private String referenceId;
    private Boolean shouldAnimateUpdate = false;
    volatile boolean running = true;
    private static Runnable addGraphicRunnable;
    private static Thread addGraphicThread;

    public RNAGSGraphicsOverlay(ReadableMap rawData, GraphicsOverlay graphicsOverlay) {
        this.referenceId = rawData.getString("referenceId");
        ReadableArray pointImageDictionaryRaw = rawData.getArray("pointGraphics");
        pointImageDictionary = new HashMap<>();
        this.graphicsOverlay = graphicsOverlay;

        for (int i = 0; i < pointImageDictionaryRaw.size(); i++) {
            ReadableMap item = pointImageDictionaryRaw.getMap(i);
            if (item.hasKey("graphicId")) {
                String graphicId = item.getString("graphicId");
                String uri = item.getMap("graphic").getString("uri");
                pointImageDictionary.put(graphicId, uri);
            }
        }
        ReadableArray rawPoints = rawData.getArray("points");
        for (int i = 0; i < rawPoints.size(); i++) {
        //force sleep each 50 time to prevent performance when back to previous screen while add graphic
        addGraphicsLoop(rawPoints.getMap(i));
        }
    }


    public void stopThread(){
      try{
        addGraphicRunnable.wait(100);
      } catch (Exception e) {
      }
      running=false;
    }

    // Getters
    public GraphicsOverlay getAGSGraphicsOverlay() {
        return graphicsOverlay;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setShouldAnimateUpdate(Boolean value) {
        shouldAnimateUpdate = value;
    }

    public void updateGraphics(ReadableArray args) {
//       Create graphics within overlay
         addGraphicRunnable=new Runnable() {
           public void run() {
           try {
               for (int i = 0; i < args.size(); i++) {
                 if(!running) {
                   break;
               }
                 updateGraphicLoop(args.getMap(i));
               }
               addGraphicRunnable.wait(100);
               cancel();
           } catch (Exception e) {
           }

           }
           public void cancel() {
           running=false;
       }
       };
       addGraphicThread=new Thread(addGraphicRunnable);
       addGraphicThread.start();
    }

    private void updateGraphicLoop(ReadableMap args) {
        // Establish variables
        com.esri.arcgisruntime.geometry.Point agsPoint = null;
        // Get references
        String referenceId = args.getString("referenceId");
        Map<String, Object> attributes = null;
        Double rotation = 0.0;

        // Once we have all the required values, we change them
        Graphic graphic = ArrayHelper.graphicViaReferenceId(graphicsOverlay, referenceId);
        if (graphic == null) {
            return;
        }

        if (args.hasKey("attributes")) {
            attributes = RNAGSGraphicsOverlay.readableMapToMap(args.getMap("attributes"));
            graphic.getAttributes().putAll(attributes);

        }
        if (args.hasKey("graphicId")) {
            String graphicId = args.getString("graphicId");
            String graphicUri = pointImageDictionary.get(graphicId);
            if (graphicUri != null) {
                PictureMarkerSymbol symbol = new PictureMarkerSymbol(graphicUri);
                graphic.setSymbol(symbol);
            }
        }
        if (args.hasKey("latitude") && args.hasKey("longitude")) {
            Double latitude = args.getDouble("latitude");
            Double longitude = args.getDouble("longitude");
            agsPoint = new com.esri.arcgisruntime.geometry.Point(longitude, latitude, SpatialReferences.getWgs84());
        }
        if (args.hasKey("rotation")) {
            rotation = args.getDouble("rotation");
        }
        if(args.hasKey("zIndex")){
          Integer zIndex=args.getInt("zIndex");
          graphic.setZIndex(zIndex);
        }
        if (shouldAnimateUpdate) {
            Float initialRotation = (graphic.getSymbol() != null && graphic.getSymbol() instanceof PictureMarkerSymbol) ?
                    ((PictureMarkerSymbol) graphic.getSymbol()).getAngle() : 0;
            animateUpdate(graphic, ((com.esri.arcgisruntime.geometry.Point) graphic.getGeometry()), agsPoint, initialRotation, rotation.floatValue());

        } else {
            graphic.setGeometry(agsPoint);
            ((PictureMarkerSymbol) graphic.getSymbol()).setAngle(rotation.floatValue());
        }
        // End of updates

    }

    private int maxTimesFired = 10;
    private int timerDuration = 500;
    private void animateUpdate(Graphic graphic, com.esri.arcgisruntime.geometry.Point fromPoint, com.esri.arcgisruntime.geometry.Point toPoint,
                               Float fromRotation, Float toRotation){
        // Run animation
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Double dx = (toPoint.getX() - fromPoint.getX()) / maxTimesFired;
                Double dy = (toPoint.getY() - fromPoint.getY()) / maxTimesFired;
                Float dTheta = (toRotation - fromRotation) / maxTimesFired;
                PictureMarkerSymbol  symbol = null;
                if (graphic.getSymbol() instanceof PictureMarkerSymbol) {
                    symbol = ((PictureMarkerSymbol) graphic.getSymbol());
                }

                for(int timesFired = 0; timesFired < maxTimesFired; timesFired++) {
                    Double x = fromPoint.getX() + (dx * timesFired);
                    Double y = fromPoint.getY() + (dy * timesFired);
                    Float rotation = fromRotation + (dTheta * timesFired);
                    graphic.setGeometry(new com.esri.arcgisruntime.geometry.Point(x,y,SpatialReferences.getWgs84()));
                    if (symbol != null) {
                        symbol.setAngle(rotation);
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                graphic.setGeometry(toPoint);
                if (symbol != null) {
                    symbol.setAngle(toRotation);
                }
                timer.cancel();
            }
        },0,timerDuration);

    }

    public void addGraphics(ReadableArray args) {
      for (int i = 0; i < args.size(); i++) {
        addGraphicsLoop(args.getMap(i));
        }


    }

    private void addGraphicsLoop(ReadableMap map) {
        Point point = Point.fromRawData(map);
        Graphic graphic = RNAGSGraphicsOverlay.rnPointToAGSGraphic(point, pointImageDictionary);
      if(map.hasKey("zIndex")){
        Integer zIndex=map.getInt("zIndex");
        graphic.setZIndex(zIndex);
      }
        graphicsOverlay.getGraphics().add(graphic);
    }

    public void removeGraphics(ReadableArray args) {
        for (int i = 0; i < args.size(); i++) {
            removeGraphicsLoop(args.getString(i));
        }
    }

    private void removeGraphicsLoop(String referenceId) {
        // Identify the graphic and remove it
        Graphic graphic = ArrayHelper.graphicViaReferenceId(graphicsOverlay, referenceId);
        if (graphic != null) {
            graphicsOverlay.getGraphics().remove(graphic);
        }
    }

    // MARK: Static methods
    public static Graphic rnPointToAGSGraphic(Point point, Map<String, String> pointImageDictionary) {
        com.esri.arcgisruntime.geometry.Point agsPoint = new com.esri.arcgisruntime.geometry.Point(point.getLongitude(), point.getLatitude(), SpatialReferences.getWgs84());
        Graphic result;
        if (point.getGraphicId() != null && pointImageDictionary.get(point.getGraphicId()) != null) {
            String imageUri = pointImageDictionary.get(point.getGraphicId());
            assert imageUri != null;
            PictureMarkerSymbol symbol = new PictureMarkerSymbol(imageUri);
            if (point.getAttributes() != null) {
                result = new Graphic(agsPoint, point.getAttributes(), symbol);
            } else {
                result = new Graphic(agsPoint, symbol);
            }
        } else {
            SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.GREEN, 10);
            if (point.getAttributes() != null) {
                result = new Graphic(agsPoint, point.getAttributes(), symbol);
            } else {
                result = new Graphic(agsPoint, symbol);
            }
        }
        result.getAttributes().put("referenceId", point.getReferenceId());
        return result;

    }

    private static Map<String, Object> readableMapToMap(ReadableMap rawMap) {
        Map<String, Object> map = new HashMap<>();
        ReadableMapKeySetIterator iterator = rawMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            map.put(key, rawMap.getString(key));
        }
        return map;
    }

    // MARK: Inner class
    public static class Point {
        private Double latitude;
        private Double longitude;
        private Double rotation;
        private String referenceId;
        private Map<String, Object> attributes;
        private String graphicId;

        public static Point fromRawData(ReadableMap rawData) {
            // Convert map to attribute map
            Map<String, Object> map = null;
            if (rawData.hasKey("attributes")) {
                ReadableMap rawMap = rawData.getMap("attributes");
                map = RNAGSGraphicsOverlay.readableMapToMap(rawMap);
            }
            Double rotation = 0.0;
            if (rawData.hasKey("rotation")) {
                rotation = rawData.getDouble("rotation");
            }
            String graphicId = "";
            if (rawData.hasKey("graphicId")) {
                graphicId = rawData.getString("graphicId");
            }
            return new Point(
                    rawData.getDouble("latitude"),
                    rawData.getDouble("longitude"),
                    rotation,
                    rawData.getString("referenceId"),
                    map,
                    graphicId
            );
        }

        private Point( Double latitude, Double longitude,  Double rotation, String referenceId,
                      Map<String, Object> attributes, String graphicId) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.rotation = rotation;
            this.referenceId = referenceId;
            this.attributes = attributes;
            this.graphicId = graphicId;
        }

        // MARK: Get/Set
        public void setRotation(Double rotation) {
            this.rotation = rotation;
        }

        public void setReferenceId(String referenceId) {
            this.referenceId = referenceId;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public void setGraphicId(String graphicId) {
            this.graphicId = graphicId;
        }

        public void setAttributes(Map<String, Object> attributes) {
            this.attributes = attributes;
        }

        public String getReferenceId() {
            return referenceId;
        }

        public String getGraphicId() {
            return graphicId;
        }

        public Map<String, Object> getAttributes() {
            return attributes;
        }

        public Double getRotation() {
            return rotation;
        }

        public double getLongitude() {
            return longitude;
        }

        public double getLatitude() {
            return latitude;
        }
    }
}
