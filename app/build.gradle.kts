plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.bhagraj.indiangirlswallpaper"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.bhagraj.indiangirlswallpaper"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
}
