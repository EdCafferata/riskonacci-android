# Riskonacci (Android)

Android port of [Riskonacci](https://github.com/EdCafferata/riskonacci), a free planning-poker app with a built-in **Risk** card type (None → Low → Medium → High → Critical) alongside Fibonacci, Standard, and T-Shirt-size decks — plus a Likelihood × Impact two-round mode that combines into a 5×5 risk matrix.

Native Kotlin + Jetpack Compose, not a cross-platform framework — mirrors the iOS app's models and screens 1:1 where the platforms allow it.

## Status (this is a work in progress, ported in one overnight session)

**Working:**
- All four decks (Fibonacci, Standard, T-Shirt, Risk), solo play
- Risk deck's two-round Likelihood × Impact flow with the combined 5×5 risk matrix reveal
- Full localization — 10 languages (nl, de, fr, es, it, pt, ja, ko, ru, zh-Hans), translations reused verbatim from the iOS app's `Localizable.xcstrings` for consistency between platforms
- Material 3 theming matching the iOS app's gold accent color exactly
- Launcher icon reused from the iOS app's app icon asset

**Not yet ported:**
- **"Play together" (local multiplayer)** — the iOS app uses Apple's MultipeerConnectivity; the Android equivalent would be Google's [Nearby Connections API](https://developers.google.com/nearby/connections/overview), which is a real implementation effort of its own, not a drop-in port. Not started yet.
- **"Online" rooms (CloudKit)** — CloudKit is Apple-only. There is no cross-platform way for an iPhone and an Android device to join the *same* online room without a shared backend (see the note below).
- **Tip jar** — the iOS app uses StoreKit 2; Android's equivalent is Google Play Billing, which requires product IDs configured in a Google Play Console listing. No Play Console account exists yet for this app (that's a paid, account-creation step only the account owner can do).
- **Rate-the-app prompt** — same reasoning; Android's equivalent (Google Play's in-app review API) also needs a Play Console listing to mean anything.

### On cross-platform multiplayer
If the goal is ever "an iPhone and an Android device play in the same room," neither MultipeerConnectivity nor CloudKit can be reused as-is — both are Apple-only. That would need a shared backend both platforms can talk to (e.g. Firebase, or a small self-hosted relay), which is a separate architecture decision from "porting the UI," not something to assume by default.

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
