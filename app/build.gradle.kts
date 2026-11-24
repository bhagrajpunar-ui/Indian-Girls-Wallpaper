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
        versionCode = 3
        versionName = "1.2"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
}
