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
    testOptions {
        // android.util.Log & co. return default values instead of throwing in local unit tests
        unitTests.isReturnDefaultValues = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(project(":domain"))

    // Init
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // DB - Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Preferences
    implementation(libs.androidx.datastore.preferences)

    // AI Agent - Groq
    implementation(libs.groq.kt)
    implementation(libs.ktor.client.android)
    // JSON
    implementation(libs.converter.gson)
    implementation(libs.kotlinx.serialization.json)
}

ksp {
    // Keep the Room schema history in the repo, needed to write and test migrations
    arg("room.schemaLocation", "$projectDir/schemas")
}
