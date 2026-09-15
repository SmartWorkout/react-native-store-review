#import "RNStoreReview.h"

#import "RNStoreReview-Swift.h"

@implementation RNStoreReview

- (dispatch_queue_t)methodQueue
{
  return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(isAvailableAsync:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
  resolve(@([StoreReview isAvailable]));
}

RCT_EXPORT_METHOD(requestReview:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
  dispatch_async(dispatch_get_main_queue(), ^{
    if ([StoreReview requestReview]) {
      // StoreKit has no completion callback or dialog visibility result.
      resolve(nil);
    } else {
      reject(@"E_NO_SCENE", @"No foreground scene to request a review.", nil);
    }
  });
}

#ifdef RCT_NEW_ARCH_ENABLED
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:(const facebook::react::ObjCTurboModule::InitParams &)params
{
  return std::make_shared<facebook::react::NativeRNStoreReviewSpecJSI>(params);
}
#endif

@end
