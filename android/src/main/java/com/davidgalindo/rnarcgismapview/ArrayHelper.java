package com.davidgalindo.rnarcgismapview;

import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;

import java.util.List;

public class ArrayHelper {
    public static Graphic graphicViaReferenceId(GraphicsOverlay graphicsOverlay, String referenceId) {
        List<Graphic> list = graphicsOverlay.getGraphics();
        for (Graphic item : list) {
            String referenceIdFromGraphic = ((String) item.getAttributes().get("referenceId"));
            if (referenceIdFromGraphic != null && referenceIdFromGraphic.equals(referenceId))
                return item;
        }
        return null;
    }
}
