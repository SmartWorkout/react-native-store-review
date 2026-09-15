# react-native-store-review

Use the familiar `expo-store-review` in-app review API in React Native without
installing Expo or Expo modules.

## Migrating from expo-store-review

For native in-app review flows using `hasAction()`, `isAvailableAsync()`, and
`requestReview()`, you can keep your existing calls and change the import:

```diff
-import * as StoreReview from 'expo-store-review';
+import * as StoreReview from 'react-native-store-review';
```

This applies when your app does not use `storeUrl()`, Expo-configured store URLs,
or the automatic store URL fallback. Native errors and platform edge cases can
differ; see [Compatibility with Expo](#compatibility-with-expo).

Replace the dependency with **this GitHub fork**. The upstream npm package with
the same name does not expose the same API.

```bash
yarn remove expo-store-review
yarn add 'react-native-store-review@https://github.com/SmartWorkout/react-native-store-review.git#a9d57725d39606d88d7cc13eb001bb8c1636b3f0'
pod install --project-directory=ios
```

Rebuild the native app after installation. A JavaScript reload alone cannot add
the native module. React Native autolinking handles native registration.

## Usage

```ts
import * as StoreReview from 'react-native-store-review';

async function requestAppReview() {
  try {
    if (await StoreReview.isAvailableAsync()) {
      await StoreReview.requestReview();
    }
  } catch (error) {
    // Availability checks and native review requests can fail.
    console.warn('Unable to request an app review', error);
  }
}
```

You can also keep an existing `hasAction()` check. In this fork it returns the
same result as `isAvailableAsync()`.

Availability and successful completion do not tell you whether the system
showed a dialog or whether the user submitted a review.

## Compatibility with Expo

The compatibility reference is `expo-store-review` 57.0.0. This fork supports its
native in-app review API, not every Expo configuration feature or error behavior.

| API | This fork | Compatibility |
| --- | --- | --- |
| `isAvailableAsync(): Promise<boolean>` | Checks Google Play on Android and detects TestFlight on iOS; false on web or when the native module is missing. | Availability logic adapted from Expo. |
| `hasAction(): Promise<boolean>` | Returns `isAvailableAsync()`. | Matches Expo when no store URLs are configured. |
| `requestReview(): Promise<void>` | Requests a native review and propagates native failures. | Same call signature; error codes and platform edge cases differ. |
| `storeUrl()` | Not provided. | Apps using this method must supply their own store links. |

The fork does not read `ios.appStoreUrl` or `android.playStoreUrl` from Expo
configuration and does not automatically redirect to a store. On web or with a
missing native module, a direct `requestReview()` call rejects instead of using
Expo's URL fallback or warning behavior.

### Android

`isAvailableAsync()` checks whether the Google Play Store package is installed.
The module includes the package visibility query needed for that check on
Android 11 and later. This does not verify account eligibility or whether a
particular installation will receive a review prompt.

`requestReview()` checks for a usable activity before requesting review info and
again before launching the flow. Its promise resolves when Google Play's launch
task completes. Failures retain native diagnostics using `E_NO_ACTIVITY`,
`E_REVIEW_REQUEST_<Google error code>`, `E_REVIEW_REQUEST`, or `E_REVIEW_LAUNCH`.
Unexpected availability check failures reject with `E_REVIEW_AVAILABILITY`.
These error codes are specific to this fork.

### iOS

`isAvailableAsync()` uses Expo's sandbox receipt and embedded provisioning
profile check to detect TestFlight. It returns true on the simulator and for
builds not detected as TestFlight. It does not check the system's review quota.

`requestReview()` runs on the main queue and requires a foreground-active window
scene. Without one, it rejects with `E_NO_SCENE`. Unlike Expo 57.0.0, it does not
fall back to a foreground-inactive scene. It uses `AppStore.requestReview` on
iOS 16 and later and `SKStoreReviewController` on earlier supported iOS versions.

StoreKit has no completion callback. The promise resolves after the request is
submitted, not after the dialog is dismissed.

### Web

`isAvailableAsync()` and `hasAction()` resolve to false. `requestReview()` rejects
without loading the native module. Keep the availability guard when sharing
application code with web.

## Troubleshooting

- **Native module missing:** install pods on iOS and rebuild the native app.
- **Available, but no dialog:** availability is only a capability check. The
  store controls whether a prompt appears; completion cannot confirm visibility.
- **TestFlight:** the availability check returns false for detected TestFlight
  builds. A production build is also not guaranteed to show a prompt.
- **Android testing:** follow Google's [in-app review testing guide](https://developer.android.com/guide/playcore/in-app-review/test).
- **Store links:** handle them explicitly in the app if needed. iOS URL scheme
  configuration for opening store links does not enable native StoreKit reviews.

## Implementation and maintenance

- Native module setup, autolinking, and bridges are based on
  [`react-native-store-review` v0.5.0](https://github.com/oblador/react-native-store-review/tree/v0.5.0).
- Google Play availability and iOS TestFlight detection are adapted from
  `expo-store-review` 57.0.0. See [third-party notices](THIRD_PARTY_NOTICES.md).
- Review requests use the upstream React Native implementation with Promise
  completion, native error reporting, and activity/scene checks added in this
  fork. This is not a full source port of Expo StoreReview.

See [FORK.md](FORK.md) for maintenance and verification. Pin a tested full commit
SHA in the consuming application's dependency.
