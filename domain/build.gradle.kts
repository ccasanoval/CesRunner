plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    //
    //alias(libs.plugins.ksp)
}

android {
    namespace = "com.cesoft.cesrunner.model"
    compileSdk = 34

    defaultConfig {
        minSdk = 30

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Init
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

//    // DI - Koin
//    implementation(libs.koinCore)
//    implementation(libs.koinAndroid)
//    implementation(libs.koinAnnotations)
//    implementation(libs.koinCompose)
//    ksp(libs.koinKspCompiler)
}