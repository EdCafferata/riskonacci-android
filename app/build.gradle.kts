plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.gms.google-services")
}

android {
    namespace = "info.cafferata.riskonacci"
    compileSdk = 36

    defaultConfig {
        applicationId = "info.cafferata.riskonacci"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.navigation:navigation-compose:2.9.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    // Firebase — the shared multiplayer backend for both "Nearby" and
    // "Online" rooms, and the only one: no separate Nearby Connections
    // implementation, since one Firestore-backed transport already works
    // for local and remote play and interoperates with the iOS app.
    implementation(platform("com.google.firebase:firebase-bom:34.15.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
