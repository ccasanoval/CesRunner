plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    //
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.cesoft.cesrunner.data"
    compileSdk = rootProject.extra.get("compileSdkVer") as Int

    defaultConfig {
        minSdk = rootProject.extra.get("minSdkVer") as Int
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
}

dependencies {
    implementation(project(":domain"))

    // Init
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // DB - Room
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Preferences
    implementation(libs.androidx.datastore.preferences)
    //implementation(libs.androidx.security.crypto)

    // DI - Hilt
//    ksp(libs.hilt.android.ksp)
//    implementation(libs.hilt.android)
//    implementation(libs.androidx.hilt.navigation.compose)
//    annotationProcessor(libs.hilt.compiler)
}