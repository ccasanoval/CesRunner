
ext["compileSdkVer"] = 35
ext["minSdkVer"] = 31

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Init
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    //
    id("com.google.dagger.hilt.android") version "2.52" apply false
}