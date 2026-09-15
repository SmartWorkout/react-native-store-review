package com.oblador.storereview;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import java.util.Map;
import java.util.HashMap;

public class StoreReviewModule extends ReactContextBaseJavaModule {

    StoreReviewModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return StoreReviewModuleImpl.NAME;
    }

    @ReactMethod
    public void isAvailableAsync(Promise promise) {
        StoreReviewModuleImpl.isAvailableAsync(getReactApplicationContext(), promise);
    }

    @ReactMethod
    public void requestReview(Promise promise) {
        StoreReviewModuleImpl.requestReview(getReactApplicationContext(), promise);
    }
}
