package com.oblador.storereview;

import androidx.annotation.NonNull;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import java.util.Map;
import java.util.HashMap;

public class StoreReviewModule extends NativeRNStoreReviewSpec {

    StoreReviewModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    @NonNull
    public String getName() {
        return StoreReviewModuleImpl.NAME;
    }

    @Override
    public void requestReview(Promise promise) {
        StoreReviewModuleImpl.requestReview(getReactApplicationContext(), promise);
    }
}
