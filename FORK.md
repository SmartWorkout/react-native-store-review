# Maintenance

Base: oblador/react-native-store-review v0.5.0 (0f41e44).
Keep the upstream MIT license and author attribution.

The fork carries the former SmartWorkout patch in source: Promise-based native
bridges and TypeScript spec, Android activity checks and error propagation,
iOS foreground-scene validation, and a web entry point.

When updating React Native or merging upstream changes:

- Review the native spec and bridges together; regenerate codegen on iOS/Android.
- Compile both native platforms and run the consuming app review helper tests.
- Check that native failures do not consume the app review cooldown.
- Test Android using a release installed through Google Play Internal Testing,
  with an eligible account and no existing review. A successful native task
  cannot confirm dialog visibility.
- iOS StoreKit provides no completion callback. TestFlight does not show reviews.
- Pin the validated full commit SHA in the consuming application's dependency.

Do not reintroduce the old patch-package patch in the consuming application.

Availability detection is adapted from expo-store-review 57.0.0; retain
THIRD_PARTY_NOTICES.md when distributing the package. Public API tests run with
`npm test` on Node.js 22.13+ (built-in TypeScript stripping and VM modules).
