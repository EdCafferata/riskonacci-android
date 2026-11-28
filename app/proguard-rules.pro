# R8-regels voor de release-build (isMinifyEnabled = true).
# Bibliotheken (Compose, AndroidX, Billing, Firebase, kotlinx-serialization) leveren hun eigen consumer-regels mee.

# Leesbare stacktraces in Play Console (de mapping zit automatisch in de AAB).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Firestore zet documenten om via reflectie (toObject / @PropertyName): velden, getters/setters
# en de no-arg-constructor moeten hun naam houden, anders breekt multiplayer met de iOS-app.
-keep class info.cafferata.riskonacci.networking.firebase.** { *; }
-keepattributes *Annotation*,Signature
