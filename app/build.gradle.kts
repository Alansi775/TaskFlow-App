// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Add this line for the google-services plugin
    id("com.google.gms.google-services") // This applies the plugin to your app module
    alias(libs.plugins.kotlin.compose) // Keep this if you want the alias, or remove if you just use buildFeatures { compose = true }
}

android {
    namespace = "com.mohammed.taskflow"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mohammed.taskflow"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { // Added this as a common good practice
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions { // Added this section
        kotlinCompilerExtensionVersion = "1.5.11" // Match your Kotlin version (e.g., 1.9.0)
    }
    packaging { // Added this section
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android & Kotlin extensions
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Jetpack Compose dependencies
    implementation(libs.androidx.activity.compose)
    implementation(platform("androidx.compose:compose-bom:2024.06.00")) // Using direct string for latest BOM
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Jetpack Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.8.0-beta02") // For Compose Navigation

    // Jetpack Compose ViewModel integration
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2") // For ViewModel in Compose

    // Firebase Platform (BoM)
    implementation(platform("com.google.firebase:firebase-bom:32.8.1")) // Using direct string for latest Firebase BOM

    // Firebase SDKs
    implementation("com.google.firebase:firebase-auth-ktx") // Firebase Authentication
    implementation("com.google.firebase:firebase-firestore-ktx") // Firebase Firestore Database

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00")) // Using direct string for Compose BOM in tests
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}