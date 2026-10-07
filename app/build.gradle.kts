// Gradle plugins used to build the Android application.
plugins {
    // Android application plugin.
    alias(libs.plugins.android.application)

    // Kotlin Compose plugin for Jetpack Compose support.
    alias(libs.plugins.kotlin.compose)

    // KSP plugin used by Room to generate database-related code.
    alias(libs.plugins.ksp)
}

android {
    // Package namespace used by generated Android and Kotlin classes.
    namespace = "com.david.guidedjournal"

    // Android SDK version used to compile the application.
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        // Unique application ID used when installing the app.
        applicationId = "com.david.guidedjournal"

        // Minimum Android version supported by the app.
        minSdk = 24

        // Android SDK version the app is designed and tested against.
        targetSdk = 37

        // Internal version number used to identify app releases.
        versionCode = 1

        // User-visible version name.
        versionName = "1.0"

        // Test runner used for Android instrumentation tests.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        // Configuration for the release version of the app.
        release {
            // Controls compiler/build optimizations for release builds.
            optimization {
                enable = false
            }
        }
    }

    // Java compatibility settings used when compiling the project.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Enables Jetpack Compose features in the Android module.
    buildFeatures {
        compose = true
    }
}

dependencies {

    // Compose Foundation provides basic layout and UI building blocks.
    implementation(libs.androidx.compose.foundation)

    // AndroidX
    // Core Android Kotlin extensions.
    implementation(libs.androidx.core.ktx)

    // Lifecycle-aware Android runtime components.
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Integration between Android activities and Jetpack Compose.
    implementation(libs.androidx.activity.compose)

    // Compose
    // Compose BOM keeps Compose library versions compatible with each other.
    implementation(platform(libs.androidx.compose.bom))

    // Core Compose UI APIs.
    implementation(libs.androidx.compose.ui)

    // Graphics-related Compose APIs.
    implementation(libs.androidx.compose.ui.graphics)

    // Tools for Compose previews in Android Studio.
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Material 3 components and theming.
    implementation(libs.androidx.compose.material3)

    // Extended Material icons for additional icons.
    implementation(libs.androidx.compose.material.icons.extended)

    // Room
    // Room runtime provides the database framework used by the app.
    implementation(libs.androidx.room.runtime)

    // Room Kotlin extensions, including coroutine support.
    implementation(libs.androidx.room.ktx)

    // Room compiler used by KSP to generate database implementation code.
    ksp(libs.androidx.room.compiler)

    // Lifecycle + ViewModel
    // Compose integration for Android ViewModel.
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Unit tests
    // JUnit support for local JVM tests.
    testImplementation(libs.junit)

    // Instrumentation tests
    // AndroidX JUnit integration for device/emulator tests.
    androidTestImplementation(libs.androidx.junit)

    // Espresso support for Android UI testing.
    androidTestImplementation(libs.androidx.espresso.core)

    // Compose BOM for consistent Compose versions in Android tests.
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Compose testing APIs for instrumented UI tests.
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Debug
    // Compose tooling used while running/debugging the app.
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Compose test manifest required by some debug UI tests.
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}