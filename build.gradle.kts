
ext["compileSdkVer"] = 36
ext["minSdkVer"] = 30

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Init
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    kotlin("plugin.serialization") version "2.0.21"
}