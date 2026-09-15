import Foundation
import StoreKit

@objc
public class StoreReview: NSObject {

  // Adapted from expo-store-review 57.0.0; see THIRD_PARTY_NOTICES.md.
  @objc
  public static func isAvailable() -> Bool {
    #if targetEnvironment(simulator)
    return true
    #else
    let isSandboxEnv = Bundle.main.appStoreReceiptURL?.lastPathComponent == "sandboxReceipt"
    let hasEmbeddedMobileProvision = Bundle.main.path(forResource: "embedded", ofType: "mobileprovision") != nil
    return !(isSandboxEnv && !hasEmbeddedMobileProvision)
    #endif
  }

  @MainActor
  @objc
  public static func requestReview() -> Bool {
    if let activeScene = UIApplication.shared.connectedScenes.first(where: { $0.activationState == .foregroundActive }) as? UIWindowScene {
      if #available(iOS 16.0, *) {
        AppStore.requestReview(in: activeScene)
      } else {
        SKStoreReviewController.requestReview(in: activeScene)
      }
      return true
    }
    return false
  }
}
