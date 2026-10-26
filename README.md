# Riskonacci (Android)

🔒 Laatste security check: 2026-10-26 20:42 CET

Android port of [Riskonacci](https://github.com/EdCafferata/riskonacci), a free planning-poker app with a built-in **Risk** card type (None → Low → Medium → High → Critical) alongside Fibonacci, Standard, and T-Shirt-size decks — plus a Likelihood × Impact two-round mode that combines into a 5×5 risk matrix.

Native Kotlin + Jetpack Compose, not a cross-platform framework — mirrors the iOS app's models and screens 1:1 where the platforms allow it.

## Status

**Feature-complete and live on Google Play** (developer "The IT Crowd"), at parity with the iOS app:
- All four decks (Fibonacci, Standard, T-Shirt, Risk), solo play
- Risk deck's two-round Likelihood × Impact flow with the combined 5×5 risk matrix reveal
- **"Play together" multiplayer, cross-platform with iOS** — backed by Firebase (Firestore + Anonymous Auth), the same shared backend and schema as the iOS app, so an iPhone and an Android device can join the exact same room with a 5-character code. See the iOS repo's README for the Firebase project setup (shared between both apps) and the Firestore security rules.
- **Tip jar** — voluntary consumable in-app purchases via Google Play Billing (`billing-ktx`), the Android counterpart of the iOS StoreKit 2 tip jar. Product IDs are configured in the Google Play Console listing.
- Full localization — 10 languages (nl, de, fr, es, it, pt, ja, ko, ru, zh-Hans), translations reused verbatim from the iOS app's `Localizable.xcstrings` for consistency between platforms
- Material 3 theming matching the iOS app's gold accent color exactly
- Launcher icon reused from the iOS app's app icon asset
- System back button steps back one level through the app (reveal → grid, grid → deck picker, out of a multiplayer room) instead of closing the app; only the deck picker (the root) exits on back, mirroring the iOS app's `NavigationStack` behaviour.
- A subtle diagonal background wash echoing the app icon's tricolore (green in the top-left → red in the bottom-right, on the logo's diagonal) with translucent "glass" panels, in both light and dark. Defined in `MainActivity.kt` (`Modifier.riskonacciLogoWash`); kept to a plain translucent treatment (no runtime blur) so it stays on `compileSdk 36` with no extra dependency.

**Platform-intentional difference (not a gap):** the Tip Jar's "Rate" button opens the Play Store listing rather than using Play's In-App Review API. Google recommends deep-linking to the listing for an explicit user-tapped "Rate" action — the In-App Review API is meant for unprompted moments and is quota-limited. (The iOS app uses `requestReview()` there, the iOS-appropriate equivalent.)

### Firebase config
This app needs its own `google-services.json` (bundle package `info.cafferata.riskonacci`) in `app/` — gitignored, same reasoning as the iOS repo's `GoogleService-Info.plist`. Ask whoever manages the Firebase project for it.

## Requirements

- JDK 17+ (project built and tested with Homebrew's `openjdk@21`)
- Android SDK, compileSdk 36, minSdk 26
- Gradle 9.6.1+ (via the included wrapper)

## Build

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools   # or your own SDK path
./gradlew assembleDebug
```

The debug APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

**Note on the project's location:** this project lives outside `/Volumes/Backup-Ed/AI/` (unlike Ed's other local project checkouts) because Gradle's file-locking/hashing doesn't work over the SMB network share those live on (`java.io.IOException: Operation not supported` when Gradle tries to hash files there) — Xcode/xcodebuild tolerate that network mount fine, Gradle does not. This repo is the durable copy.

## Licence

GPL-3.0 — see [LICENSE](LICENSE), same as the iOS app.
