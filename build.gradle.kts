// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google() // Google's Maven repository, essential for Firebase plugins
        mavenCentral() // Maven Central repository
    }
    dependencies {
        // This line adds the Google Services plugin to your project's classpath.
        // It must be here so the app/build.gradle.kts can find it.
        classpath("com.google.gms:google-services:4.4.1") // Use the latest stable version
    }
}

plugins {
    // These aliases declare plugins that are applied to sub-modules (like :app)
    // 'apply false' means they are declared here for resolution, but not applied to the root project itself.
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false // Keep this if you have it in libs.versions.toml
}