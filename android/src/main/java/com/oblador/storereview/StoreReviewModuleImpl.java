package com.oblador.storereview;

import android.app.Activity;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.UiThreadUtil;
import com.google.android.play.core.review.ReviewException;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.gms.tasks.Task;

public class StoreReviewModuleImpl {
    public static final String NAME = "RNStoreReview";

    public static void requestReview(ReactApplicationContext context, Promise promise) {
        UiThreadUtil.runOnUiThread(() -> {
            Activity activity = context.getCurrentActivity();
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                promise.reject("E_NO_ACTIVITY", "No active activity to request a review.");
                return;
            }
            try {
                ReviewManager manager = ReviewManagerFactory.create(context);
                Task<ReviewInfo> request = manager.requestReviewFlow();
                request.addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Exception error = task.getException();
                        String code = error instanceof ReviewException
                            ? "E_REVIEW_REQUEST_" + ((ReviewException) error).getErrorCode()
                            : "E_REVIEW_REQUEST";
                        promise.reject(code, "Failed to request Google Play review info.", error);
                        return;
                    }
                    Activity currentActivity = context.getCurrentActivity();
                    if (currentActivity == null || currentActivity.isFinishing() || currentActivity.isDestroyed()) {
                        promise.reject("E_NO_ACTIVITY", "Activity disappeared before launching the review.");
                        return;
                    }
                    try {
                        manager.launchReviewFlow(currentActivity, task.getResult())
                            .addOnCompleteListener(flow -> {
                                if (flow.isSuccessful()) {
                                    // Google does not disclose whether the dialog was shown.
                                    promise.resolve(null);
                                } else {
                                    promise.reject("E_REVIEW_LAUNCH", "Google Play review flow failed.", flow.getException());
                                }
                            });
                    } catch (Exception error) {
                        promise.reject("E_REVIEW_LAUNCH", "Unable to launch Google Play review.", error);
                    }
                });
            } catch (Exception error) {
                promise.reject("E_REVIEW_REQUEST", "Unable to request Google Play review info.", error);
            }
        });
    }
}
