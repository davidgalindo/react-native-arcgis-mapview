package com.davidgalindo.rnarcgismapview;

import android.view.View;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.uimanager.UIManagerModule;

public class RNArcGISMapViewModule extends ReactContextBaseJavaModule {
    public RNArcGISMapViewModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "RNArcGISMapViewManager";
    }

    @ReactMethod
    public void getRouteIsVisible(final int viewId, final Callback callback) {
        UIManagerModule uiManagerModule = getReactApplicationContext().getNativeModule(UIManagerModule.class);
        uiManagerModule.addUIBlock(nativeViewHierarchyManager -> {
            View view = nativeViewHierarchyManager.resolveView(viewId);
            if (view instanceof RNAGSMapView) {
                Boolean result = ((RNAGSMapView) view).getRouteIsVisible();
                callback.invoke(result);
            }
        });
    }

    @ReactMethod
    public void setLicenseKey(String licenseKey) {
        ArcGISRuntimeEnvironment.setLicense(licenseKey);
    }
}
