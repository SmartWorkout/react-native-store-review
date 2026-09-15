# react-native-store-review (SmartWorkout fork)

An Expo-free React Native module with the familiar `expo-store-review` in-app
review API.

- **Native module setup and bridge:** `react-native-store-review` v0.5.0
  (`0f41e44`), including React Native autolinking and architecture support.
- **Availability implementation:** adapted from `expo-store-review` 57.0.0,
  including Google Play detection and the iOS TestFlight check.
- **Review requests:** the upstream React Native implementation extended with
  Promise completion, native error reporting, and activity/scene checks.

No Expo runtime or Expo modules are required. See
[third-party notices](THIRD_PARTY_NOTICES.md) for the adapted Expo code.

## Migrating from expo-store-review

**For an in-app review flow like SmartWorkout's, the only application code change
is the import.** Existing calls to `hasAction()`, `isAvailableAsync()`, and
`requestReview()` can stay as they are:

```diff
-import * as StoreReview from 'expo-store-review';
+import * as StoreReview from 'react-native-store-review';
```

First replace the dependency with **this GitHub fork** (the npm upstream package
does not provide this API), install iOS pods, and rebuild the native app:

```bash
yarn remove expo-store-review
yarn add 'react-native-store-review@https://github.com/SmartWorkout/react-native-store-review.git#a9d57725d39606d88d7cc13eb001bb8c1636b3f0'
pod install --project-directory=ios
```

This import-only migration applies when your app uses those three methods for
native in-app reviews and does not rely on Expo's configured store URLs or its
automatic store redirect. That is the setup used by SmartWorkout.

### Compatibility scope

- `isAvailableAsync()` uses the availability checks adapted from Expo.
- `hasAction()` returns `isAvailableAsync()`. It does not inspect
  `ios.appStoreUrl` or `android.playStoreUrl` in Expo configuration.
- `requestReview()` returns `Promise<void>`. Native completion and errors follow
  the behavior described below; identical error codes and all edge-case behavior
  across the two libraries are not guaranteed.
- `storeUrl()` and Expo's automatic URL fallback are not implemented. Apps that
  use them need to manage store links explicitly.
- On web, availability is false and a direct `requestReview()` call rejects.
  Guard the request with the availability check.

## Fork behavior

`requestReview(): Promise<void>` rejects native failures on both platforms.
Android resolves when the Google Play launch task completes. iOS resolves after
submitting the request to StoreKit, which offers no completion callback.
Neither result confirms that the dialog appeared or a review was submitted.

Android errors include `E_NO_ACTIVITY`, `E_REVIEW_REQUEST_<Google error code>`,
`E_REVIEW_REQUEST`, and `E_REVIEW_LAUNCH`. iOS rejects with `E_NO_SCENE` when no
foreground window scene is available. Calls are dispatched to the main thread.
Web rejects without loading the native module. There are no automatic retries
or store redirects.

Install this fork from GitHub and pin the full commit SHA in the consuming app.
See [FORK.md](FORK.md) for maintenance and verification.

This module exposes the native APIs to ask the user to rate the app in the iOS App Store or Google Play store directly from within the app (requires iOS >= 14.0 or Android 5.0 with Google Play store installed). 

<img width="274" alt="Rating Dialog" src="https://cloud.githubusercontent.com/assets/378279/24377493/d22eb0b8-133f-11e7-9968-44d186a3801f.png">

## Availability

```ts
if (await StoreReview.isAvailableAsync()) {
  await StoreReview.requestReview();
}
```

- Android: checks whether Google Play Store is installed. The manifest includes
  its package visibility query for Android 11 and later.
- iOS: returns false for TestFlight, using Expo's sandbox receipt and embedded
  provisioning profile check. Returns true on the simulator and for development
  builds that are not detected as TestFlight.
- Web or missing native module: returns false.

Availability does not guarantee that the system will show a review dialog.
`hasAction()` is also available and returns the same result as
`isAvailableAsync()`. This matches Expo when no store URLs are configured, as in
SmartWorkout. The fork does not read Expo app configuration; store links remain
the app's responsibility.

## Installation

```bash
# Add dependency
yarn add react-native-store-review@https://github.com/SmartWorkout/react-native-store-review.git#<full-commit-sha>
# Link iOS dependency
pod install --project-directory=ios
# Compile project
react-native run-ios # or run-android
```

## Usage

The intention of this API is to ask the user to rate the app as a part of the user journey, typically as the user completes a task. **Since it's not possible to know if a dialog will be shown or not you should not call it as a result of tapping a button**, but rather as a side effect of an event happening in the app. 

```js
import * as StoreReview from 'react-native-store-review';

try {
  await StoreReview.requestReview();
} catch (error) {
  // Report native failure; do not record a completed request.
  console.warn(error);
}
```

### Button

If you want to show a button or provide a fallback for OS versions not supporting these APIs, you can redirect the user to the respective stores to review the app there instead. 

```js
import { Linking, Platform } from 'react-native';

const APP_STORE_LINK = `itms-apps://apps.apple.com/app/id${IOS_APP_ID}?action=write-review`;
const PLAY_STORE_LINK = `market://details?id=${ANDROID_APP_ID}`;

const STORE_LINK = Platform.select({
  ios: APP_STORE_LINK,
  android: PLAY_STORE_LINK,
});

export const openReviewInStore = () => Linking.openURL(STORE_LINK)
```

## References

* [`SKStoreReviewController` for App Store](https://developer.apple.com/documentation/storekit/skstorereviewcontroller/requesting_app_store_reviews) 
* [`In-App Review API` for Google Play Store](https://developer.android.com/guide/playcore/in-app-review).

## Troubleshooting

### The dialog is not showing in the correct language on iOS

The strings in the dialog comes from the OS, if your translations are purely in JavaScript land you need to add meta data so iOS understand which languages you support, [see the official documentation](https://developer.apple.com/documentation/xcode/localization/adding_support_for_languages_and_regions).

### The dialog is not showing when I call `requestReview()`

##### (1)
For iOS you have to add LSApplicationQueriesSchemes as Array param to Info.plist and add itms-apps as one of params in this array to link appstore.

For example:
```js
<key>LSApplicationQueriesSchemes</key>
<array>
  <string>itms-apps</string>
</array>
```

##### or (2)
The dialog **is not showing while testing with TestFlight** but will be working normally once in production ([source](https://stackoverflow.com/questions/46770549/skstorereviewcontroller-requestreview-popup-is-not-showing-in-testflight-build/47048474#47048474)). Furthermore it will not work for enterprise apps as they are not available on the App Store, and Apple/Google will restrict the amount of times the API can be called to a few times per year in order prevent misuse. 
